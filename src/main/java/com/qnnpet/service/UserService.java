package com.qnnpet.service;

import com.qnnpet.dto.CreateTeacherRequest;
import com.qnnpet.entity.SysUser;

import java.util.List;

public interface UserService {

    List<SysUser> listTeachers();

    Object createTeacher(CreateTeacherRequest request);

    void updateTeacherStatus(Long id, Integer status);

    String resetPassword(Long id);
}