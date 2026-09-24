package com.qnnpet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("score_log")
public class ScoreLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("class_id")
    private Long classId;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("rule_name")
    private String ruleName;

    private String type;

    private Integer score;

    private String remark;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;
}