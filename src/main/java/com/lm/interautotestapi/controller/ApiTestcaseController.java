package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.ApiTestcase;
import com.lm.interautotestapi.service.ApiTestcaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/testcase")
@RequiredArgsConstructor
public class ApiTestcaseController {

    private final ApiTestcaseService apiTestcaseService;

    @GetMapping("/page")
    @SaCheckPermission("case:manage")
    public Result<Page<ApiTestcase>> page(@RequestParam(defaultValue = "1") int pageNum,
                                          @RequestParam(defaultValue = "10") int pageSize,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String env) {
        Page<ApiTestcase> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ApiTestcase> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ApiTestcase::getCaseTitle, keyword);
        }
        if (env != null && !env.isEmpty()) {
            wrapper.eq(ApiTestcase::getEnv, env);
        }
        wrapper.orderByDesc(ApiTestcase::getId);
        return Result.ok(apiTestcaseService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("case:manage")
    public Result<ApiTestcase> getById(@PathVariable Long id) {
        return Result.ok(apiTestcaseService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("case:manage")
    public Result<Void> save(@RequestBody ApiTestcase apiTestcase) {
        apiTestcaseService.save(apiTestcase);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("case:manage")
    public Result<Void> update(@RequestBody ApiTestcase apiTestcase) {
        apiTestcaseService.updateById(apiTestcase);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("case:manage")
    public Result<Void> delete(@PathVariable Long id) {
        apiTestcaseService.removeById(id);
        return Result.ok();
    }
}
