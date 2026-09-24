package com.qnnpet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("class_info")
public class ClassInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("teacher_id")
    private Long teacherId;

    private String name;

    private String grade;

    private String semester;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}