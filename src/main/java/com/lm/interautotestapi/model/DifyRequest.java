package com.lm.interautotestapi.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DifyRequest {

    @NotBlank(message = "输入内容不能为空")
    private String query;

    private String responseMode = "blocking";

    private String user = "admin";
}
