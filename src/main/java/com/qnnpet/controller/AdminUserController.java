package com.qnnpet.controller;

import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.common.Result;
import com.qnnpet.dto.CreateTeacherRequest;
import com.qnnpet.entity.SysUser;
import com.qnnpet.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员 - 老师管理
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public Result<List<SysUser>> list() {
        log.info("管理员查询老师列表");
        return Result.success(userService.listTeachers());
    }

    
    @PostMapping
    public Result<Object> create(@Valid @RequestBody CreateTeacherRequest request) {
        log.info("管理员创建老师: username={}, realName={}", request.getUsername(), request.getRealName());
        Object result = userService.createTeacher(request);
        log.info("老师创建成功: username={}", request.getUsername());
        return Result.success(result);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值必须为 0 或 1");
        }
        log.info("管理员更新老师状态: id={}, status={}", id, status);
        userService.updateTeacherStatus(id, status);
        return Result.success();
    }

    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        log.info("管理员重置老师密码: id={}", id);
        return Result.success(userService.resetPassword(id));
    }
}
