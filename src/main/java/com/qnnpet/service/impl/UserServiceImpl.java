package com.qnnpet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qnnpet.common.BusinessException;
import com.qnnpet.common.ErrorCode;
import com.qnnpet.dto.CreateTeacherRequest;
import com.qnnpet.entity.SysUser;
import com.qnnpet.mapper.SysUserMapper;
import com.qnnpet.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    @Override
    public List<SysUser> listTeachers() {
        log.info("查询老师列表");
        return sysUserMapper.selectList(
                new QueryWrapper<SysUser>()
                        .eq("role", "teacher")
                        .orderByDesc("created_at"));
    }

    @Override
    public Object createTeacher(CreateTeacherRequest request) {
        SysUser existing = sysUserMapper.selectByUsername(request.getUsername());
        if (existing != null) {
            log.warn("创建老师失败-用户名已存在: username={}", request.getUsername());
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
        log.info("老师创建成功: id={}, username={}, realName={}",
                teacher.getId(), teacher.getUsername(), teacher.getRealName());
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
                log.warn("禁用自己失败-不能禁用自己的账号: currentUserId={}", currentUserId);
                throw new BusinessException(ErrorCode.FORBIDDEN, "不能禁用自己的账号");
            }
        }
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            log.warn("操作失败-不能修改管理员账号: id={}, role={}", id, user.getRole());
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能修改管理员账号");
        }
        user.setStatus(status);
        sysUserMapper.updateById(user);
        log.info("老师状态更新成功: id={}, username={}, newStatus={}", id, user.getUsername(), status);
    }

    @Override
    public String resetPassword(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            log.warn("操作失败-不能重置管理员密码: id={}", id);
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能重置管理员密码");
        }
        String rawPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));
        sysUserMapper.updateById(user);
        log.info("老师密码重置成功: id={}, username={}", id, user.getUsername());
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
