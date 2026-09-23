package com.qnnpet.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.Result;
import com.qnnpet.entity.Pet;
import com.qnnpet.entity.PetLevelConfig;
import com.qnnpet.entity.PetType;
import com.qnnpet.entity.Student;
import com.qnnpet.mapper.PetLevelConfigMapper;
import com.qnnpet.mapper.PetMapper;
import com.qnnpet.mapper.PetTypeMapper;
import com.qnnpet.mapper.StudentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 课堂大屏展示（PRD §5.9）
 */
@RestController
@RequestMapping("/api/teacher/display")
@RequiredArgsConstructor
public class DisplayController {

    private final StudentMapper studentMapper;
    private final PetMapper petMapper;
    private final PetTypeMapper petTypeMapper;
    private final PetLevelConfigMapper petLevelConfigMapper;

    @GetMapping("/{classId}")
    public Result<List<Map<String, Object>>> display(@PathVariable Long classId) {
        List<Student> students = studentMapper.selectList(
                new QueryWrapper<Student>().eq("class_id", classId).orderByAsc("sort_order"));
        if (students.isEmpty()) {
            return Result.success(List.of());
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
                row.put("customName", p.getCustomName());
                row.put("currentLevel", p.getCurrentLevel());
                row.put("currentScore", p.getCurrentScore());
                PetLevelConfig lvl = petLevelConfigMapper.selectOne(
                        new QueryWrapper<PetLevelConfig>()
                                .eq("pet_type_id", p.getPetTypeId())
                                .eq("level", p.getCurrentLevel())
                                .last("LIMIT 1"));
                row.put("levelName", lvl != null ? lvl.getLevelName() : null);
                row.put("imageUrl", lvl != null ? lvl.getImageUrl() : null);
            } else {
                row.put("petId", null);
            }
            result.add(row);
        }
        return Result.success(result);
    }
}