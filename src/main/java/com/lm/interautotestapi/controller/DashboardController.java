package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.ApiInterface;
import com.lm.interautotestapi.entity.ApiTestcase;
import com.lm.interautotestapi.service.ApiInterfaceService;
import com.lm.interautotestapi.service.ApiTestcaseService;
import com.lm.interautotestapi.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ApiInterfaceService apiInterfaceService;
    private final ApiTestcaseService apiTestcaseService;
    private final SysUserService sysUserService;

    @GetMapping("/stats")
    @SaCheckPermission("api:manage")
    public Result<Map<String, Object>> stats() {
        Map<String, Object> result = new LinkedHashMap<>();

        long totalInterfaces = apiInterfaceService.count();
        long totalTestcases = apiTestcaseService.count();
        long totalUsers = sysUserService.count();
        long enabledInterfaces = apiInterfaceService.count(
                new LambdaQueryWrapper<ApiInterface>().eq(ApiInterface::getEnabled, 1));

        result.put("totalInterfaces", totalInterfaces);
        result.put("totalTestcases", totalTestcases);
        result.put("totalUsers", totalUsers);
        result.put("enabledInterfaces", enabledInterfaces);

        List<ApiInterface> allInterfaces = apiInterfaceService.list(
                new LambdaQueryWrapper<ApiInterface>()
                        .select(ApiInterface::getId, ApiInterface::getApiName, ApiInterface::getMethod)
                        .orderByDesc(ApiInterface::getId));
        List<Long> interfaceIds = allInterfaces.stream().map(ApiInterface::getId).collect(Collectors.toList());

        Map<Long, Long> tcCountMap = Collections.emptyMap();
        if (!interfaceIds.isEmpty()) {
            QueryWrapper<ApiTestcase> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("interface_id", "COUNT(*) AS cnt")
                    .in("interface_id", interfaceIds)
                    .groupBy("interface_id");
            List<Map<String, Object>> countList = apiTestcaseService.getBaseMapper().selectMaps(queryWrapper);
            tcCountMap = new HashMap<>();
            for (Map<String, Object> row : countList) {
                Object ifaceIdObj = row.get("interface_id");
                Object cntObj = row.get("cnt");
                if (ifaceIdObj != null) {
                    Long ifaceId = ifaceIdObj instanceof Long ? (Long) ifaceIdObj : Long.valueOf(ifaceIdObj.toString());
                    Long cnt = cntObj != null ? (cntObj instanceof Long ? (Long) cntObj : Long.valueOf(cntObj.toString())) : 0L;
                    tcCountMap.put(ifaceId, cnt);
                }
            }
        }

        List<Map<String, Object>> interfaceTcStats = new ArrayList<>();
        List<String> chartNames = new ArrayList<>();
        List<Integer> chartCounts = new ArrayList<>();
        List<Long> chartIds = new ArrayList<>();
        for (ApiInterface iface : allInterfaces) {
            long count = tcCountMap.getOrDefault(iface.getId(), 0L);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("apiName", iface.getApiName());
            item.put("method", iface.getMethod());
            item.put("testcaseCount", count);
            interfaceTcStats.add(item);
            chartNames.add(iface.getApiName());
            chartCounts.add((int) count);
            chartIds.add(iface.getId());
        }
        result.put("interfaceTcStats", interfaceTcStats);
        result.put("chartNames", chartNames);
        result.put("chartCounts", chartCounts);
        result.put("chartIds", chartIds);

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
