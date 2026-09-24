package com.qnnpet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pet_type")
public class PetType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    @TableField("sort_order")
    private Integer sortOrder;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;
}