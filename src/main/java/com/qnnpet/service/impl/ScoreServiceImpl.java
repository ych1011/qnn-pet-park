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
 */
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

    @Override
    @Transactional
    public AddScoreResponse addScore(AddScoreRequest request, Long teacherId) {
        AddScoreResponse cached = checkIdempotent(request.getIdempotentKey());
        if (cached != null) {
            return cached;
        }
        Student student = studentMapper.selectById(request.getStudentId());
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        ClassInfo cls = classInfoMapper.selectById(student.getClassId());
        if (cls == null || !cls.getTeacherId().equals(teacherId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作其他老师的学生");
        }
        Pet pet = petMapper.selectOne(
                new QueryWrapper<Pet>().eq("student_id", request.getStudentId()));
        if (pet == null) {
            throw new BusinessException(ErrorCode.CONFLICT, "该学生尚未分配宠物，无法记录积分");
        }
        int oldScore = pet.getCurrentScore() == null ? 0 : pet.getCurrentScore();
        int oldLevel = pet.getCurrentLevel() == null ? 1 : pet.getCurrentLevel();
        int delta = "add".equals(request.getType()) ? request.getScore() : -request.getScore();
        int newScore = oldScore + delta;
        boolean forceNoDegrade = false;
        if (newScore < 0) {
            newScore = 0;
            forceNoDegrade = true;
        }
        int newLevel = forceNoDegrade ? oldLevel : calculateLevel(pet.getPetTypeId(), newScore);
        pet.setCurrentScore(newScore);
        pet.setCurrentLevel(newLevel);
        petMapper.updateById(pet);

        ScoreLog log = new ScoreLog();
        log.setStudentId(request.getStudentId());
        log.setClassId(cls.getId());
        log.setRuleId(request.getRuleId());
        log.setRuleName(request.getRuleName());
        log.setType(request.getType());
        log.setScore(request.getScore());
        log.setRemark(request.getRemark());
        scoreLogMapper.insert(log);

        boolean leveledUp = newLevel > oldLevel;
        AddScoreResponse resp = new AddScoreResponse(
                log.getId(), newScore, newLevel, leveledUp, leveledUp ? oldLevel : null);
        cacheIdempotent(request.getIdempotentKey(), resp);
        return resp;
    }

    @Override
    @Transactional
    public void undoLatestScore(Long teacherId) {
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
                throw new BusinessException(ErrorCode.FORBIDDEN, "撤销已超时（仅10秒内可撤销）");
            }
        }
        Pet pet = petMapper.selectOne(
                new QueryWrapper<Pet>().eq("student_id", latest.getStudentId()));
        if (pet != null) {
            int currentScore = pet.getCurrentScore() == null ? 0 : pet.getCurrentScore();
            int delta = "add".equals(latest.getType()) ? -latest.getScore() : latest.getScore();
            int newScore = currentScore + delta;
            if (newScore < 0) newScore = 0;
            pet.setCurrentScore(newScore);
            pet.setCurrentLevel(calculateLevel(pet.getPetTypeId(), newScore));
            petMapper.updateById(pet);
        }
        scoreLogMapper.deleteById(latest.getId());
    }

    @Override
    public List<Map<String, Object>> getRanking(Long classId) {
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
        return ranking;
    }

    @Override
    public Map<String, Object> getScoreLogs(Long studentId, Integer page, Integer size) {
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

    private static class IdempotentEntry {
        final AddScoreResponse response;
        final long timestamp;

        IdempotentEntry(AddScoreResponse response, long timestamp) {
            this.response = response;
            this.timestamp = timestamp;
        }
    }
}