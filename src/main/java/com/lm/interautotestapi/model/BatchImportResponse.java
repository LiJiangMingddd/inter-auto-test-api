package com.lm.interautotestapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量导入响应")
public class BatchImportResponse {

    @Schema(description = "是否全部成功")
    private boolean success;

    @Schema(description = "成功导入的接口数")
    private int interfaceSuccess;

    @Schema(description = "失败数")
    private int interfaceFailed;

    @Schema(description = "成功导入的用例数")
    private int testcaseSuccess;

    @Schema(description = "失败详情列表")
    private List<ImportError> errors;

    @Data
    @Schema(description = "导入错误详情")
    public static class ImportError {
        @Schema(description = "索引位置（从0开始）")
        private int index;

        @Schema(description = "接口名称")
        private String apiName;

        @Schema(description = "错误原因")
        private String reason;
    }
}
