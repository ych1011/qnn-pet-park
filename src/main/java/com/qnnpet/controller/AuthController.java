package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.LoginRequest;
import com.qnnpet.dto.LoginResponse;
import com.qnnpet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证模块
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        LoginResponse resp = authService.login(request);
        log.info("用户登录成功: username={}, role={}", request.getUsername(), resp.getRole());
        return Result.success(resp);
    }

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

    @GetMapping("/me")
    public Result<Object> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        log.info("查询当前用户信息: userId={}", userId);
        return Result.success(authService.getCurrentUser(userId));
    }
}
