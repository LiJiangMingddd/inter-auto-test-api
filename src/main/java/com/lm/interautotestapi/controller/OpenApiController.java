package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.model.BatchImportRequest;
import com.lm.interautotestapi.model.BatchImportResponse;
import com.lm.interautotestapi.service.OpenApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/open")
@Tag(name = "Open API（对外接口）", description = "需要先调用 /api/auth/getToken 获取 token，再在 Authorization 头携带 token 调用本接口。")
public class OpenApiController {

    @Resource
    private OpenApiService openApiService;

    @PostMapping("/batch-import")
    @SaCheckLogin
    @Operation(summary = "批量导入接口和测试用例",
            description = "步骤1: POST /api/auth/getToken 传入 {appId, appKey} 获取 token\n步骤2: 在 Authorization 头携带 token 调用本接口批量导入")
    public Result<BatchImportResponse> batchImport(
            @Valid @RequestBody BatchImportRequest request) {
        log.info("▶▶▶ [OpenApi] 收到批量导入请求：interfaces={} 条", request.getInterfaces().size());
        BatchImportResponse response = openApiService.batchImport(request);
        return Result.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "供 Agent 检查服务是否可用")
    public Result<String> health() {
        return Result.ok("OK - Inter Auto Test API is running");
    }
}
