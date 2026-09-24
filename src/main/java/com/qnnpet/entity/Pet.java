package com.qnnpet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("student_id")
    private Long studentId;

    @TableField("pet_type_id")
    private Long petTypeId;

    @TableField("custom_name")
    private String customName;

    @TableField("current_level")
    private Integer currentLevel;

    @TableField("current_score")
    private Integer currentScore;

    @Version
    @TableField("version")
    private Integer version;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}