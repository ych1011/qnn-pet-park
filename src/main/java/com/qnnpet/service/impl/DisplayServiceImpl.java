package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.PetLevelConfig;
import com.qnnpet.entity.PetType;
import com.qnnpet.entity.Student;
import com.qnnpet.mapper.ClassInfoMapper;
import com.qnnpet.mapper.PetLevelConfigMapper;
import com.qnnpet.mapper.PetMapper;
import com.qnnpet.mapper.PetTypeMapper;
import com.qnnpet.mapper.StudentMapper;
import com.qnnpet.service.DisplayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 课堂大屏展示服务实现（PRD §5.9）
 * - 数据归属校验：teacher 只能查询自己班级的大屏
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisplayServiceImpl implements DisplayService {

    private final StudentMapper studentMapper;
    private final PetMapper petMapper;
    private final PetTypeMapper petTypeMapper;
    private final PetLevelConfigMapper petLevelConfigMapper;
    private final ClassInfoMapper classInfoMapper;

    @Override
    public List<Map<String, Object>> getDisplayData(Long classId, Long teacherId) {
        log.info("查询大屏展示数据: classId={}, teacherId={}", classId, teacherId);
        ClassInfo cls = classInfoMapper.selectById(classId);
        if (cls == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        if (!cls.getTeacherId().equals(teacherId)) {
            log.warn("越权访问大屏数据: classId={}, teacherId={}, 班级归属={}",
                    classId, teacherId, cls.getTeacherId());
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权查看其他班级的大屏");
        }

        List<Student> students = studentMapper.selectList(
                new QueryWrapper<Student>().eq("class_id", classId).orderByAsc("sort_order"));
        if (students.isEmpty()) {
            log.info("班级无学生数据: classId={}", classId);
            return List.of();
        }

        List<Long> studentIds = new ArrayList<>();
        Map<Long, Student> studentMap = new HashMap<>();
        for (Student s : students) {
            studentIds.add(s.getId());
            studentMap.put(s.getId(), s);
        }

        List<Pet> pets = petMapper.selectList(
                new QueryWrapper<Pet>().in("student_id", studentIds));
        Map<Long, Pet> petMap = new HashMap<>();
        Set<Long> petTypeIds = new HashSet<>();
        for (Pet p : pets) {
            petMap.put(p.getStudentId(), p);
            if (p.getPetTypeId() != null) {
                petTypeIds.add(p.getPetTypeId());
            }
        }

        Map<Long, PetType> typeMap = new HashMap<>();
        if (!petTypeIds.isEmpty()) {
            for (PetType t : petTypeMapper.selectBatchIds(petTypeIds)) {
                typeMap.put(t.getId(), t);
            }
        }

        Map<String, PetLevelConfig> levelConfigMap = new HashMap<>();
        if (!petTypeIds.isEmpty()) {
            List<PetLevelConfig> configs = petLevelConfigMapper.selectList(
                    new QueryWrapper<PetLevelConfig>().in("pet_type_id", petTypeIds));
            for (PetLevelConfig c : configs) {
                levelConfigMap.put(c.getPetTypeId() + "_" + c.getLevel(), c);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Student s : students) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("studentId", s.getId());
            row.put("studentName", s.getName());
            Pet p = petMap.get(s.getId());
            if (p != null) {
                row.put("petId", p.getId());
                row.put("petTypeId", p.getPetTypeId());
                PetType pt = typeMap.get(p.getPetTypeId());
                row.put("petTypeName", pt != null ? pt.getName() : null);
                row.put("petTypeCode", pt != null ? pt.getCode() : null);
                row.put("customName", p.getCustomName());
                row.put("currentLevel", p.getCurrentLevel());
                row.put("currentScore", p.getCurrentScore());
                PetLevelConfig lvl = levelConfigMap.get(p.getPetTypeId() + "_" + p.getCurrentLevel());
                row.put("levelName", lvl != null ? lvl.getLevelName() : null);
                row.put("imageUrl", lvl != null ? lvl.getImageUrl() : null);
            } else {
                row.put("petId", null);
            }
            result.add(row);
        }
        log.info("大屏数据查询完成: classId={}, 学生数={}", classId, result.size());
        return result;
    }
}
