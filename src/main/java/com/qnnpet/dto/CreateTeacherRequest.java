package com.qnnpet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateTeacherRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50字符，字母数字下划线")
    private String username;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 1, max = 50, message = "真实姓名长度1-50字符")
    private String realName;
}