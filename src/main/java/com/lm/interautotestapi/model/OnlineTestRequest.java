package com.lm.interautotestapi.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class OnlineTestRequest {

    @NotNull(message = "用例ID不能为空")
    private Long testcaseId;

    private String env;

    private String overrideUrl;
}
