package com.qnnpet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStudentRequest {

    @NotBlank(message = "学生姓名不能为空")
    @Size(min = 1, max = 50, message = "学生姓名长度1-50字符")
    private String name;

    private Integer sortOrder;
}