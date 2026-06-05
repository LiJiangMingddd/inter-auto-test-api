package com.lm.interautotestapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@Schema(description = "批量导入请求（需先通过 /api/auth/getToken 获取 token）")
public class BatchImportRequest {

    @NotEmpty(message = "interfaces 列表不能为空")
    @Valid
    @Schema(description = "接口列表")
    private List<BatchInterfaceItem> interfaces;
}
