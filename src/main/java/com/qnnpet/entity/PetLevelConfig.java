package com.qnnpet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_level_config")
public class PetLevelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("pet_type_id")
    private Long petTypeId;

    private Integer level;

    @TableField("level_name")
    private String levelName;

    @TableField("required_score")
    private Integer requiredScore;

    @TableField("image_url")
    private String imageUrl;

    @TableField("created_at")
    private LocalDateTime createdAt;
}