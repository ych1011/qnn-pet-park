package com.qnnpet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AssignPetRequest {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    @NotNull(message = "宠物类型ID不能为空")
    private Long petTypeId;

    @Size(max = 50, message = "宠物名字最长50字符")
    private String customName;
}