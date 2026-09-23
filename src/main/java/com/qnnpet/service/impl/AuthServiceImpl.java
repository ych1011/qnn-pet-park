package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.dto.LoginRequest;
import com.qnnpet.dto.LoginResponse;
import com.qnnpet.entity.ClassInfo;
import com.qnnpet.entity.SysUser;
import com.qnnpet.mapper.ClassInfoMapper;
import com.qnnpet.mapper.SysUserMapper;
import com.qnnpet.security.JwtTokenProvider;
import com.qnnpet.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务实现（PRD §5.1）
 * - BCrypt cost=10 校验密码
 * - JWT 24h
 * - 密码错误不区分用户名/密码
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final ClassInfoMapper classInfoMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "账号已被禁用，请联系管理员");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), user.getRole());
        boolean hasClass = false;
        if ("teacher".equals(user.getRole())) {
            ClassInfo cls = classInfoMapper.selectOne(
                    new QueryWrapper<ClassInfo>().eq("teacher_id", user.getId()));
            hasClass = cls != null;
        }
        return new LoginResponse(token, user.getRole(), user.getRealName(), hasClass);
    }

    @Override
    public void logout(String token) {
        // JWT 无状态认证，V1 不维护服务端黑名单
    }

    @Override
    public Object getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("realName", user.getRealName());
        info.put("role", user.getRole());
        info.put("status", user.getStatus());
        return info;
    }
}