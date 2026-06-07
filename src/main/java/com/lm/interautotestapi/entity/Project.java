package com.lm.interautotestapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String projectName;

    private String projectCode;

    private String description;

    private Long ownerId;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private String ownerName;

    @TableField(exist = false)
    private Integer memberCount;

    @TableField(exist = false)
    private Integer interfaceCount;
}
