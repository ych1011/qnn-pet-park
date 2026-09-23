package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.CreateStudentRequest;
import com.qnnpet.entity.Student;
import com.qnnpet.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生管理
 */
@RestController
@RequestMapping("/api/teacher/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public Result<List<Student>> list(@RequestParam Long classId) {
        return Result.success(studentService.listStudents(classId));
    }

    @PostMapping
    public Result<Student> create(@Valid @RequestBody CreateStudentRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(studentService.createStudent(request, teacherId));
    }

    @PutMapping("/{id}")
    public Result<Student> update(@PathVariable Long id, @Valid @RequestBody CreateStudentRequest request, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        return Result.success(studentService.updateStudent(id, request, teacherId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long teacherId = (Long) authentication.getPrincipal();
        studentService.deleteStudent(id, teacherId);
        return Result.success();
    }
}