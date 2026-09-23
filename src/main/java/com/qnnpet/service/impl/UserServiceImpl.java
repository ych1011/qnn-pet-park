package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.dto.CreateTeacherRequest;
import com.qnnpet.entity.SysUser;
import com.qnnpet.mapper.SysUserMapper;
import com.qnnpet.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 老师管理服务实现（PRD §5.2）
 * - 6 位随机初始密码
 * - 不能禁用自己
 * - 禁用后数据保留
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    @Override
    public List<SysUser> listTeachers() {
        return sysUserMapper.selectList(
                new QueryWrapper<SysUser>()
                        .eq("role", "teacher")
                        .orderByDesc("created_at"));
    }

    @Override
    public Object createTeacher(CreateTeacherRequest request) {
        SysUser existing = sysUserMapper.selectByUsername(request.getUsername());
        if (existing != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "该用户名已被使用");
        }
        String rawPassword = generateRandomPassword();
        SysUser teacher = new SysUser();
        teacher.setUsername(request.getUsername());
        teacher.setRealName(request.getRealName());
        teacher.setPassword(passwordEncoder.encode(rawPassword));
        teacher.setRole("teacher");
        teacher.setStatus(1);
        sysUserMapper.insert(teacher);
        Map<String, Object> result = new HashMap<>();
        result.put("id", teacher.getId());
        result.put("username", teacher.getUsername());
        result.put("realName", teacher.getRealName());
        result.put("initialPassword", rawPassword);
        return result;
    }

    @Override
    public void updateTeacherStatus(Long id, Integer status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long) {
            Long currentUserId = (Long) auth.getPrincipal();
            if (id.equals(currentUserId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "不能禁用自己的账号");
            }
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
    }

    @Override
    public String resetPassword(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        String rawPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        sysUserMapper.updateById(user);
        return rawPassword;
    }

    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}