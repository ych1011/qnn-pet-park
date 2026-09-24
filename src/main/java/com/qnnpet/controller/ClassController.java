package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.service.ClassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 班级管理
 */
@Slf4j
@RestController
@RequestMapping("/api/teacher/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping("/current")
    public Result<ClassInfo> current(Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询当前班级: teacherId={}", teacherId);
        return Result.success(classService.getCurrentClass(teacherId));
    }

    @PostMapping
    public Result<ClassInfo> create(@Valid @RequestBody ClassInfo classInfo, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("创建班级: teacherId={}, name={}", teacherId, classInfo.getName());
        ClassInfo result = classService.createClass(classInfo, teacherId);
        log.info("班级创建成功: id={}, teacherId={}", result.getId(), teacherId);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    public Result<ClassInfo> update(@PathVariable Long id, @Valid @RequestBody ClassInfo classInfo,
                                    Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("更新班级: id={}, teacherId={}", id, teacherId);
        return Result.success(classService.updateClass(id, classInfo, teacherId));
    }
}
