package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.service.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 班级管理
 */
@RestController
@RequestMapping("/api/teacher/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping("/current")
    public Result<ClassInfo> current(Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(classService.getCurrentClass(teacherId));
    }

    @PostMapping
    public Result<ClassInfo> create(@RequestBody ClassInfo classInfo, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(classService.createClass(classInfo, teacherId));
    }

    @PutMapping("/{id}")
    public Result<ClassInfo> update(@PathVariable Long id, @RequestBody ClassInfo classInfo, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(classService.updateClass(id, classInfo, teacherId));
    }
}