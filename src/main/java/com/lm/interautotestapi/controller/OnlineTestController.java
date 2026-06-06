package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.model.OnlineTestRequest;
import com.lm.interautotestapi.model.OnlineTestResponse;
import com.lm.interautotestapi.service.OnlineTestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/online-test")
public class OnlineTestController {

    @Resource
    private OnlineTestService onlineTestService;

    @PostMapping("/execute")
    @SaCheckPermission("case:manage")
    public Result<OnlineTestResponse> execute(@Valid @RequestBody OnlineTestRequest request) {
        log.info("[OnlineTest] 收到在线测试请求: testcaseId={}, env={}", request.getTestcaseId(), request.getEnv());
        OnlineTestResponse response = onlineTestService.executeTest(request);
        return Result.ok(response);
    }
}
