package com.qnnpet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddScoreRequest {

    @NotNull(message = "学生ID不能为空")
    private Long studentId;

    private Long ruleId;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotNull(message = "分值不能为空")
    @Min(value = 1, message = "分值最小1")
    @Max(value = 99, message = "分值最大99")
    private Integer score;

    @NotBlank(message = "规则名称不能为空")
    @Size(min = 1, max = 100, message = "规则名称长度1-100字符")
    private String ruleName;

    @Size(max = 255, message = "备注最长255字符")
    private String remark;

    @NotBlank(message = "幂等键不能为空")
    private String idempotentKey;
}