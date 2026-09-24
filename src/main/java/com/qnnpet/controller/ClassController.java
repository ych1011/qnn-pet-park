package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 班级管理
 */
@Slf4j
@Tag(name = "老师-班级管理", description = "老师创建/编辑自己的班级，创建时自动初始化默认规则")
@RestController
@RequestMapping("/api/teacher/classes")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @Operation(summary = "查询当前老师的班级", description = "返回当前登录老师所属班级")
    @GetMapping("/current")
    public Result<ClassInfo> current(Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询当前班级: teacherId={}", teacherId);
        return Result.success(classService.getCurrentClass(teacherId));
    }

    @Operation(summary = "创建班级", description = "每个老师只能创建一个班级；创建时自动初始化 10 条默认积分规则")
    @PostMapping
    public Result<ClassInfo> create(@Valid @RequestBody ClassInfo classInfo, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("创建班级: teacherId={}, name={}", teacherId, classInfo.getName());
        ClassInfo result = classService.createClass(classInfo, teacherId);
        log.info("班级创建成功: id={}, teacherId={}", result.getId(), teacherId);
        return Result.success(result);
    }

    @Operation(summary = "编辑班级", description = "更新班级名称/年级/学期")
    @PutMapping("/{id}")
    public Result<ClassInfo> update(@PathVariable Long id, @Valid @RequestBody ClassInfo classInfo,
                                    Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("更新班级: id={}, teacherId={}", id, teacherId);
        return Result.success(classService.updateClass(id, classInfo, teacherId));
    }
}