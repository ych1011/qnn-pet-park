package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.LoginRequest;
import com.qnnpet.dto.LoginResponse;
import com.qnnpet.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证模块
 */
@Slf4j
@Tag(name = "认证模块", description = "登录、登出、当前用户信息")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录", description = "用户名密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        LoginResponse resp = authService.login(request);
        log.info("用户登录成功: username={}, role={}", request.getUsername(), resp.getRole());
        return Result.success(resp);
    }

    @Operation(summary = "登出", description = "JWT 无状态，仅客户端丢弃 Token 即可")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader,
                               Authentication authentication) {
        if (authentication != null) {
            log.info("用户登出: userId={}", authentication.getPrincipal());
        }
        if (authHeader != null) {
            authService.logout(authHeader);
        }
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息", description = "根据 Token 解析当前登录用户")
    @GetMapping("/me")
    public Result<Object> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("查询当前用户信息: userId={}", userId);
        return Result.success(authService.getCurrentUser(userId));
    }
}