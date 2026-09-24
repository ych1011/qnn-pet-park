package com.qnnpet.controller;

import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.common.Result;
import com.qnnpet.dto.CreateTeacherRequest;
import com.qnnpet.entity.SysUser;
import com.qnnpet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员 - 老师管理
 */
@Slf4j
@Tag(name = "管理员-老师管理", description = "管理员查看/创建老师账号、禁用/重置密码")
@RestController
@RequestMapping("/api/admin/teachers")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "查询老师列表", description = "管理员查看所有老师账号")
    @GetMapping
    public Result<List<SysUser>> list() {
        log.info("管理员查询老师列表");
        return Result.success(userService.listTeachers());
    }

    @Operation(summary = "创建老师", description = "管理员新建老师账号，返回 6 位随机初始密码")
    @PostMapping
    public Result<Object> create(@Valid @RequestBody CreateTeacherRequest request) {
        log.info("管理员创建老师: username={}, realName={}", request.getUsername(), request.getRealName());
        Object result = userService.createTeacher(request);
        log.info("老师创建成功: username={}", request.getUsername());
        return Result.success(result);
    }

    @Operation(summary = "更新老师状态", description = "启用/禁用老师账号（status: 1-启用 0-禁用），不能禁用自己")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "状态值必须为 0 或 1");
        }
        log.info("管理员更新老师状态: id={}, status={}", id, status);
        userService.updateTeacherStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "重置老师密码", description = "生成 6 位随机新密码并返回")
    @PostMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        log.info("管理员重置老师密码: id={}", id);
        return Result.success(userService.resetPassword(id));
    }
}