package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.CreateStudentRequest;
import com.qnnpet.entity.Student;
import com.qnnpet.service.StudentService;
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
@RestController
@RequestMapping("/api/teacher/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public Result<List<Student>> list(@RequestParam Long classId, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("查询学生列表: classId={}, teacherId={}", classId, teacherId);
        return Result.success(studentService.listStudents(classId, teacherId));
    }

    @PostMapping
    public Result<Student> create(@Valid @RequestBody CreateStudentRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("添加学生: teacherId={}, name={}", teacherId, request.getName());
        return Result.success(studentService.createStudent(request, teacherId));
    }

    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Long id, @Valid @RequestBody CreateStudentRequest request,
                                   Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("编辑学生: id={}, teacherId={}", id, teacherId);
        return Result.success(studentService.updateStudent(id, request, teacherId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        log.info("删除学生: id={}, teacherId={}", id, teacherId);
        studentService.deleteStudent(id, teacherId);
        return Result.success();
    }
}
