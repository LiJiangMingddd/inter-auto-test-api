package com.lm.interautotestapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

@Data
@Schema(description = "批量导入的接口项及其关联用例")
public class BatchInterfaceItem {

    @NotBlank(message = "接口名称 apiName 不能为空")
    @Schema(description = "接口名称", example = "获取用户信息", required = true)
    private String apiName;

    @Schema(description = "接口描述", example = "根据用户ID获取用户详细信息")
    private String apiInfo;

    @NotBlank(message = "HTTP 方法 method 不能为空")
    @Pattern(regexp = "GET|POST|PUT|DELETE|PATCH", message = "method 必须是 GET/POST/PUT/DELETE/PATCH 之一")
    @Schema(description = "HTTP 方法", example = "POST", required = true)
    private String method;

    @Schema(description = "DEV 环境地址", example = "http://dev-api.example.com/user/info")
    private String urlDev;

    @Schema(description = "UAT 环境地址", example = "http://uat-api.example.com/user/info")
    private String urlUat;

    @Schema(description = "PRO 环境地址", example = "http://api.example.com/user/info")
    private String urlPro;

    @Schema(description = "服务编码", example = "user-service")
    private String serviceCode;

    @Schema(description = "是否启用（1=启用, 0=禁用）", example = "1")
    private Integer enabled = 1;

    @Valid
    @Schema(description = "关联的测试用例列表")
    private List<BatchTestcaseItem> testcases;
}
