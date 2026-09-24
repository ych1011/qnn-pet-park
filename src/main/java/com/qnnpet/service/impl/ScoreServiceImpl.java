package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.dto.AddScoreRequest;
import com.qnnpet.dto.AddScoreResponse;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.PetLevelConfig;
import com.qnnpet.entity.ScoreLog;
import com.qnnpet.entity.Student;
import com.qnnpet.mapper.ClassInfoMapper;
import com.qnnpet.mapper.PetLevelConfigMapper;
import com.qnnpet.mapper.PetMapper;
import com.qnnpet.mapper.ScoreLogMapper;
import com.qnnpet.mapper.StudentMapper;
import com.qnnpet.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 积分操作服务实现（PRD §5.7 核心）
 * - 加减分更新 pet.current_score 并重算 current_level
 * - 扣分后为负则归 0，等级不降级
 * - 撤销最近一次（10 秒内）
 * - 幂等键 TTL 5 分钟
 * - 数据归属校验：teacher 只能操作自己班级的学生
 * - 乐观锁并发控制：pet 表并发加减分自动重试（max 3 次）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreLogMapper scoreLogMapper;
    private final StudentMapper studentMapper;
    private final PetMapper petMapper;
    private final ClassInfoMapper classInfoMapper;
    private final PetLevelConfigMapper petLevelConfigMapper;

    private static final ConcurrentHashMap<String, IdempotentEntry> IDEMPOTENT_CACHE = new ConcurrentHashMap<>();
    private static final long IDEMPOTENT_TTL_MS = 5 * 60 * 1000L;
    private static final int UNDO_WINDOW_SECONDS = 10;
    private static final int MAX_OPTIMISTIC_RETRY = 3;
    private static final long RETRY_INTERVAL_MS = 50L;

    @Override
    @Transactional
    public AddScoreResponse addScore(AddScoreRequest request, Long teacherId) {
        log.info("积分操作: teacherId={}, studentId={}, type={}, score={}, ruleName={}, idempotentKey={}",
                teacherId, request.getStudentId(), request.getType(), request.getScore(),
                request.getRuleName(), request.getIdempotentKey());
        if (!"add".equals(request.getType()) && !"subtract".equals(request.getType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "类型必须为 add 或 subtract");
        }
        AddScoreResponse cached = checkIdempotent(request.getIdempotentKey());
        if (cached != null) {
            log.info("命中幂等缓存，返回上次结果: idempotentKey={}", request.getIdempotentKey());
            return cached;
        }
        Student student = studentMapper.selectById(request.getStudentId());
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        ClassInfo cls = classInfoMapper.selectById(student.getClassId());
        if (cls == null || !cls.getTeacherId().equals(teacherId)) {
            log.warn("越权操作学生积分: studentId={}, teacherId={}, 班级归属={}",
                    request.getStudentId(), teacherId, cls != null ? cls.getTeacherId() : null);
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作其他老师的学生");
        }

        // 乐观锁重试：并发加减分时 version 冲突自动重试（max 3 次，间隔 50ms）
        Pet pet = null;
        int oldScore = 0;
        int oldLevel = 1;
        int newScore = 0;
        int newLevel = 1;
        int delta = "add".equals(request.getType()) ? request.getScore() : -request.getScore();
        for (int attempt = 1; attempt <= MAX_OPTIMISTIC_RETRY; attempt++) {
            pet = petMapper.selectOne(
                    new QueryWrapper<Pet>().eq("student_id", request.getStudentId()));
            if (pet == null) {
                throw new BusinessException(ErrorCode.CONFLICT, "该学生尚未分配宠物，无法记录积分");
            }
            oldScore = pet.getCurrentScore() == null ? 0 : pet.getCurrentScore();
            oldLevel = pet.getCurrentLevel() == null ? 1 : pet.getCurrentLevel();
            newScore = oldScore + delta;
            boolean forceNoDegrade = false;
            if (newScore < 0) {
                newScore = 0;
                forceNoDegrade = true;
                log.info("扣分后归零（不降级）: studentId={}, oldScore={}, delta={}",
                        request.getStudentId(), oldScore, delta);
            }
            newLevel = forceNoDegrade ? oldLevel : calculateLevel(pet.getPetTypeId(), newScore);
            pet.setCurrentScore(newScore);
            pet.setCurrentLevel(newLevel);
            int rows = petMapper.updateById(pet);
            if (rows > 0) {
                break;
            }
            if (attempt < MAX_OPTIMISTIC_RETRY) {
                log.warn("积分更新乐观锁冲突，重试: studentId={}, attempt={}/{}",
                        request.getStudentId(), attempt, MAX_OPTIMISTIC_RETRY);
                sleepRetry();
            } else {
                log.error("积分更新乐观锁冲突，重试耗尽: studentId={}", request.getStudentId());
                throw new BusinessException(ErrorCode.CONFLICT, "操作繁忙，请稍后重试");
            }
        }

        ScoreLog scoreLog = new ScoreLog();
        scoreLog.setStudentId(request.getStudentId());
        scoreLog.setClassId(cls.getId());
        scoreLog.setRuleId(request.getRuleId());
        scoreLog.setRuleName(request.getRuleName());
        scoreLog.setType(request.getType());
        scoreLog.setScore(request.getScore());
        scoreLog.setRemark(request.getRemark());
        scoreLogMapper.insert(scoreLog);

        boolean leveledUp = newLevel > oldLevel;
        AddScoreResponse resp = new AddScoreResponse(
                scoreLog.getId(), newScore, newLevel, leveledUp, leveledUp ? oldLevel : null);
        cacheIdempotent(request.getIdempotentKey(), resp);
        log.info("积分操作完成: logId={}, studentId={}, oldScore={}, newScore={}, oldLevel={}, newLevel={}, leveledUp={}",
                scoreLog.getId(), request.getStudentId(), oldScore, newScore, oldLevel, newLevel, leveledUp);
        if (leveledUp) {
            log.info("宠物升级！studentId={}, petId={}, {} 级 -> {} 级",
                    request.getStudentId(), pet.getId(), oldLevel, newLevel);
        }
        return resp;
    }

    @Override
    @Transactional
    public void undoLatestScore(Long teacherId) {
        log.info("撤销最近一次积分操作: teacherId={}", teacherId);
        ClassInfo cls = classInfoMapper.selectOne(
                new QueryWrapper<ClassInfo>().eq("teacher_id", teacherId));
        if (cls == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "请先创建班级");
        }
        ScoreLog latest = scoreLogMapper.selectOne(
                new QueryWrapper<ScoreLog>()
                        .eq("class_id", cls.getId())
                        .orderByDesc("created_at")
                        .last("LIMIT 1"));
        if (latest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "无可撤销的操作");
        }
        if (latest.getCreatedAt() != null) {
            long elapsed = Duration.between(latest.getCreatedAt(), LocalDateTime.now()).getSeconds();
            if (elapsed > UNDO_WINDOW_SECONDS) {
                log.warn("撤销失败-超时: logId={}, elapsed={}s, window={}s",
                        latest.getId(), elapsed, UNDO_WINDOW_SECONDS);
                throw new BusinessException(ErrorCode.FORBIDDEN, "撤销已超时（仅10秒内可撤销）");
            }
        }
        // 乐观锁重试：并发撤销时 version 冲突自动重试（max 3 次，间隔 50ms）
        for (int attempt = 1; attempt <= MAX_OPTIMISTIC_RETRY; attempt++) {
            Pet pet = petMapper.selectOne(
                    new QueryWrapper<Pet>().eq("student_id", latest.getStudentId()));
            if (pet == null) {
                break;
            }
            int currentScore = pet.getCurrentScore() == null ? 0 : pet.getCurrentScore();
            int delta = "add".equals(latest.getType()) ? -latest.getScore() : latest.getScore();
            int newScore = currentScore + delta;
            if (newScore < 0) newScore = 0;
            pet.setCurrentScore(newScore);
            pet.setCurrentLevel(calculateLevel(pet.getPetTypeId(), newScore));
            int rows = petMapper.updateById(pet);
            if (rows > 0) {
                break;
            }
            if (attempt < MAX_OPTIMISTIC_RETRY) {
                log.warn("撤销更新乐观锁冲突，重试: studentId={}, attempt={}/{}",
                        latest.getStudentId(), attempt, MAX_OPTIMISTIC_RETRY);
                sleepRetry();
            } else {
                log.error("撤销更新乐观锁冲突，重试耗尽: studentId={}", latest.getStudentId());
                throw new BusinessException(ErrorCode.CONFLICT, "操作繁忙，请稍后重试");
            }
        }
        scoreLogMapper.deleteById(latest.getId()); // MyBatis-Plus @TableLogic: 自动转为 UPDATE score_log SET deleted=1
        log.info("撤销成功: logId={}, studentId={}", latest.getId(), latest.getStudentId());
    }

    @Override
    public List<Map<String, Object>> getRanking(Long classId, Long teacherId) {
        log.info("查询积分排行: classId={}, teacherId={}", classId, teacherId);
        checkOwnership(classId, teacherId);
        List<Student> students = studentMapper.selectList(
                new QueryWrapper<Student>().eq("class_id", classId).orderByAsc("sort_order"));
        if (students.isEmpty()) {
            return List.of();
        }
        List<Long> studentIds = new ArrayList<>();
        Map<Long, Student> studentMap = new HashMap<>();
        for (Student s : students) {
            studentIds.add(s.getId());
            studentMap.put(s.getId(), s);
        }
        List<Pet> pets = petMapper.selectList(
                new QueryWrapper<Pet>().in("student_id", studentIds).orderByDesc("current_score"));
        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Pet p : pets) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", rank++);
            row.put("studentId", p.getStudentId());
            Student s = studentMap.get(p.getStudentId());
            row.put("studentName", s != null ? s.getName() : null);
            row.put("petTypeId", p.getPetTypeId());
            row.put("customName", p.getCustomName());
            row.put("currentLevel", p.getCurrentLevel());
            row.put("currentScore", p.getCurrentScore());
            ranking.add(row);
        }
        log.info("排行查询完成: classId={}, 学生数={}", classId, ranking.size());
        return ranking;
    }

    @Override
    public Map<String, Object> getScoreLogs(Long studentId, Integer page, Integer size, Long teacherId) {
        log.info("查询积分记录: studentId={}, page={}, size={}, teacherId={}", studentId, page, size, teacherId);
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        checkOwnership(student.getClassId(), teacherId);
        Page<ScoreLog> p = new Page<>(page, size);
        var result = scoreLogMapper.selectPage(p,
                new QueryWrapper<ScoreLog>()
                        .eq("student_id", studentId)
                        .orderByDesc("created_at"));
        Map<String, Object> resp = new HashMap<>();
        resp.put("total", result.getTotal());
        resp.put("page", page);
        resp.put("size", size);
        resp.put("list", result.getRecords());
        log.info("积分记录查询完成: studentId={}, total={}", studentId, result.getTotal());
        return resp;
    }

    private int calculateLevel(Long petTypeId, int score) {
        PetLevelConfig cfg = petLevelConfigMapper.selectOne(
                new QueryWrapper<PetLevelConfig>()
                        .eq("pet_type_id", petTypeId)
                        .le("required_score", score)
                        .orderByDesc("level")
                        .last("LIMIT 1"));
        return cfg != null ? cfg.getLevel() : 1;
    }

    private AddScoreResponse checkIdempotent(String key) {
        if (key == null) {
            return null;
        }
        IdempotentEntry entry = IDEMPOTENT_CACHE.get(key);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() - entry.timestamp > IDEMPOTENT_TTL_MS) {
            IDEMPOTENT_CACHE.remove(key);
            return null;
        }
        return entry.response;
    }

    private void cacheIdempotent(String key, AddScoreResponse resp) {
        if (key != null) {
            IDEMPOTENT_CACHE.put(key, new IdempotentEntry(resp, System.currentTimeMillis()));
        }
    }

    private void checkOwnership(Long classId, Long teacherId) {
        ClassInfo cls = classInfoMapper.selectById(classId);
        if (cls == null || !cls.getTeacherId().equals(teacherId)) {
            log.warn("越权查询积分数据: classId={}, teacherId={}", classId, teacherId);
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看其他班级的积分");
        }
    }

    /**
     * 乐观锁冲突重试间隔休眠
     */
    private void sleepRetry() {
        try {
            Thread.sleep(RETRY_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class IdempotentEntry {
        final AddScoreResponse response;
        final long timestamp;

        IdempotentEntry(AddScoreResponse response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }
    }
}