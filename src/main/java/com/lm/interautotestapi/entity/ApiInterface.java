package com.lm.interautotestapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("api_interface")
public class ApiInterface {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String apiName;

    private String apiInfo;

    private String urlDev;

    private String urlUat;

    private String urlPro;

    private String method;

    private String serviceCode;

    private Integer enabled;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
