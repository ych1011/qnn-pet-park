package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.CreateTeacherRequest;
import com.qnnpet.entity.SysUser;
import com.qnnpet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员 - 老师管理
 */
@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public Result<List<SysUser>> list() {
        return Result.success(userService.listTeachers());
    }

    @PostMapping
    public Result<Object> create(@Valid @RequestBody CreateTeacherRequest request) {
        return Result.success(userService.createTeacher(request));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateTeacherStatus(id, status);
        return Result.success();
    }

    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        return Result.success(userService.resetPassword(id));
    }
}