package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.model.DifyRequest;
import com.lm.interautotestapi.model.DifyResponse;
import com.lm.interautotestapi.service.DifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/dify")
public class DifyController {

    @Resource
    private DifyService difyService;

    @PostMapping("/run")
    @SaCheckPermission("api:manage")
    public Result<DifyResponse> run(@Valid @RequestBody DifyRequest request) {
        log.info("[Dify] 收到工作流执行请求: query={}", request.getQuery());
        DifyResponse response = difyService.executeWorkflow(request);
        return Result.ok(response);
    }
}
