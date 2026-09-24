package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.CreateStudentRequest;
import com.qnnpet.entity.Student;
import com.qnnpet.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生管理
 */
@Slf4j
@Tag(name = "老师-学生管理", description = "学生增删改查；删除学生级联删除宠物和积分记录")
@RestController
@RequestMapping("/api/teacher/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "查询学生列表", description = "按班级查询学生；只能查询自己班级的学生")
    @GetMapping
    public Result<List<Student>> list(@RequestParam Long classId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询学生列表: classId={}, teacherId={}", classId, teacherId);
        return Result.success(studentService.listStudents(classId, teacherId));
    }

    @Operation(summary = "添加学生", description = "往当前老师的班级添加学生")
    @PostMapping
    public Result<Student> create(@Valid @RequestBody CreateStudentRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("添加学生: teacherId={}, name={}", teacherId, request.getName());
        return Result.success(studentService.createStudent(request, teacherId));
    }

    @Operation(summary = "编辑学生", description = "更新学生姓名和排序")
    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Long id, @Valid @RequestBody CreateStudentRequest request,
                                   Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("编辑学生: id={}, teacherId={}", id, teacherId);
        return Result.success(studentService.updateStudent(id, request, teacherId));
    }

    @Operation(summary = "删除学生", description = "删除学生并级联删除宠物和积分记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("删除学生: id={}, teacherId={}", id, teacherId);
        studentService.deleteStudent(id, teacherId);
        return Result.success();
    }
}