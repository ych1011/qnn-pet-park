package com.qnnpet.controller;

import com.qnnpet.common.Result;
import com.qnnpet.dto.LoginRequest;
import com.qnnpet.dto.LoginResponse;
import com.qnnpet.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证模块
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        authService.logout(authHeader);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<Object> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.success(authService.getCurrentUser(userId));
    }
}