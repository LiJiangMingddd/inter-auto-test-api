package com.lm.interautotestapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@Schema(description = "测试用例")
public class BatchTestcaseItem {

    @NotBlank(message = "用例标题 caseTitle 不能为空")
    @Schema(description = "用例标题", example = "正常获取用户信息", required = true)
    private String caseTitle;

    @Schema(description = "请求数据（JSON 字符串）", example = "{\"userId\": 1}")
    private String caseData;

    @Schema(description = "断言规则", example = "$.code == 200")
    private String checkRules;

    @Schema(description = "期望结果", example = "{\"code\":200,\"msg\":\"success\"}")
    private String expectedResults;

    @Pattern(regexp = "dev|uat|pro", message = "环境 env 必须是 dev/uat/pro 之一")
    @Schema(description = "运行环境", example = "uat")
    private String env = "dev";

    @Schema(description = "是否启用（1=启用, 0=禁用）", example = "1")
    private Integer enabled = 1;

    @Schema(description = "排序值", example = "1")
    private Integer sortOrder = 0;
}
