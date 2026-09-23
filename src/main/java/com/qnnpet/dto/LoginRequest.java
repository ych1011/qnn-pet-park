package com.qnnpet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 50, message = "用户名长度1-50字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 1, max = 100, message = "密码长度1-100字符")
    private String password;
}