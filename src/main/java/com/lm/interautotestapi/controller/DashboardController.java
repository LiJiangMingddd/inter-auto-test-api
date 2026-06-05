package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.ApiInterface;
import com.lm.interautotestapi.entity.ApiTestcase;
import com.lm.interautotestapi.entity.SysUser;
import com.lm.interautotestapi.service.ApiInterfaceService;
import com.lm.interautotestapi.service.ApiTestcaseService;
import com.lm.interautotestapi.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Resource
    private ApiInterfaceService apiInterfaceService;

    @Resource
    private ApiTestcaseService apiTestcaseService;

    @Resource
    private SysUserService sysUserService;

    /**
     * 总体统计概览
     */
    @GetMapping("/stats")
    @SaCheckPermission("api:manage")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        // 1. 总数统计
        long totalInterfaces = apiInterfaceService.count();
        long totalTestcases = apiTestcaseService.count();
        long totalUsers = sysUserService.count();
        long enabledInterfaces = apiInterfaceService.count(
                new LambdaQueryWrapper<ApiInterface>().eq(ApiInterface::getEnabled, 1));

        result.put("totalInterfaces", totalInterfaces);
        result.put("totalTestcases", totalTestcases);
        result.put("totalUsers", totalUsers);
        result.put("enabledInterfaces", enabledInterfaces);

        // 2. 每个接口的用例数量（用于图表）
        List<ApiInterface> allInterfaces = apiInterfaceService.list(
                new LambdaQueryWrapper<ApiInterface>().orderByDesc(ApiInterface::getId));
        List<Map<String, Object>> interfaceTcStats = new ArrayList<>();
        List<String> chartNames = new ArrayList<>();
        List<Integer> chartCounts = new ArrayList<>();
        for (ApiInterface iface : allInterfaces) {
            long count = apiTestcaseService.count(
                    new LambdaQueryWrapper<ApiTestcase>().eq(ApiTestcase::getInterfaceId, iface.getId()));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("apiName", iface.getApiName());
            item.put("method", iface.getMethod());
            item.put("testcaseCount", count);
            interfaceTcStats.add(item);
            chartNames.add(iface.getApiName());
            chartCounts.add((int) count);
        }
        result.put("interfaceTcStats", interfaceTcStats);
        result.put("chartNames", chartNames);
        result.put("chartCounts", chartCounts);

        // 3. 按环境统计用例数
        long devCount = apiTestcaseService.count(
                new LambdaQueryWrapper<ApiTestcase>().eq(ApiTestcase::getEnv, "dev"));
        long uatCount = apiTestcaseService.count(
                new LambdaQueryWrapper<ApiTestcase>().eq(ApiTestcase::getEnv, "uat"));
        long proCount = apiTestcaseService.count(
                new LambdaQueryWrapper<ApiTestcase>().eq(ApiTestcase::getEnv, "pro"));
        result.put("envDev", devCount);
        result.put("envUat", uatCount);
        result.put("envPro", proCount);

        return Result.ok(result);
    }
}
