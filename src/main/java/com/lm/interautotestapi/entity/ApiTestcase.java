package com.lm.interautotestapi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("api_testcase")
public class ApiTestcase {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long projectId;

    private Long interfaceId;

    private String caseTitle;

    private String caseData;

    private String checkRules;

    private String expectedResults;

    private String env;

    private Integer enabled;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
