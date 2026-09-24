package com.qnnpet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 积分规则请求 DTO（PRD §5.6）
 * - 分值 1-99 正整数
 * - 类型 add/subtract
 * - 分类 study/discipline/habit/morality
 */
@Data
public class RuleRequest {

    @NotNull(message = "班级ID不能为空")
    private Long classId;

    @NotBlank(message = "规则名称不能为空")
    @Size(min = 1, max = 100, message = "规则名称长度1-100字符")
    private String name;

    @NotBlank(message = "类型不能为空")
    @Pattern(regexp = "^(add|subtract)$", message = "类型必须为 add 或 subtract")
    private String type;

    @NotNull(message = "分值不能为空")
    @Min(value = 1, message = "分值最小1")
    @Max(value = 99, message = "分值最大99")
    private Integer score;

    @NotBlank(message = "分类不能为空")
    @Pattern(regexp = "^(study|discipline|habit|morality)$", message = "分类必须为 study/discipline/habit/morality")
    private String category;

    private Integer sortOrder;

    private Integer status;
}
