package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.ApiInterface;
import com.lm.interautotestapi.entity.ApiTestcase;
import com.lm.interautotestapi.service.ApiInterfaceService;
import com.lm.interautotestapi.service.ApiTestcaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/project/{projectId}/interface")
@RequiredArgsConstructor
public class ApiInterfaceController {

    private final ApiInterfaceService apiInterfaceService;
    private final ApiTestcaseService apiTestcaseService;

    @GetMapping("/page")
    @SaCheckPermission("api:manage")
    public Result<Page<ApiInterface>> page(@PathVariable Long projectId,
                                           @RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String method,
                                           @RequestParam(required = false) String env) {
        Page<ApiInterface> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ApiInterface> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApiInterface::getProjectId, projectId);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ApiInterface::getApiName, keyword)
                    .or().like(ApiInterface::getApiInfo, keyword));
        }
        if (method != null && !method.isEmpty()) {
            wrapper.eq(ApiInterface::getMethod, method);
        }
        if (env != null && !env.isEmpty()) {
            switch (env) {
                case "dev":
                    wrapper.isNotNull(ApiInterface::getUrlDev).ne(ApiInterface::getUrlDev, "");
                    break;
                case "uat":
                    wrapper.isNotNull(ApiInterface::getUrlUat).ne(ApiInterface::getUrlUat, "");
                    break;
                case "pro":
                    wrapper.isNotNull(ApiInterface::getUrlPro).ne(ApiInterface::getUrlPro, "");
                    break;
                default:
                    break;
            }
        }
        wrapper.orderByDesc(ApiInterface::getId);
        Page<ApiInterface> result = apiInterfaceService.page(page, wrapper);
        List<Long> ids = result.getRecords().stream().map(ApiInterface::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            QueryWrapper<ApiTestcase> tcQuery = new QueryWrapper<>();
            tcQuery.select("interface_id", "COUNT(*) AS cnt").in("interface_id", ids).groupBy("interface_id");
            List<Map<String, Object>> countList = apiTestcaseService.getBaseMapper().selectMaps(tcQuery);
            Map<Long, Integer> tcCountMap = new HashMap<>();
            for (Map<String, Object> row : countList) {
                Object ifaceIdObj = row.get("interface_id");
                Object cntObj = row.get("cnt");
                if (ifaceIdObj != null) {
                    Long ifaceId = ifaceIdObj instanceof Long ? (Long) ifaceIdObj : Long.valueOf(ifaceIdObj.toString());
                    int cnt = cntObj != null ? (cntObj instanceof Long ? ((Long) cntObj).intValue() : Integer.parseInt(cntObj.toString())) : 0;
                    tcCountMap.put(ifaceId, cnt);
                }
            }
            for (ApiInterface iface : result.getRecords()) {
                iface.setTestcaseCount(tcCountMap.getOrDefault(iface.getId(), 0));
            }
        }
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("api:manage")
    public Result<ApiInterface> getById(@PathVariable Long projectId, @PathVariable Long id) {
        return Result.ok(apiInterfaceService.getById(id));
    }

    /**
     * 接口详情：包含关联的测试用例列表
     */
    @GetMapping("/{id}/detail")
    @SaCheckPermission("api:manage")
    public Result<Map<String, Object>> detail(@PathVariable Long projectId, @PathVariable Long id) {
        ApiInterface apiInterface = apiInterfaceService.getById(id);
        if (apiInterface == null) {
            return Result.fail("接口不存在");
        }
        List<ApiTestcase> testcases = apiTestcaseService.list(
                new LambdaQueryWrapper<ApiTestcase>()
                        .eq(ApiTestcase::getProjectId, projectId)
                        .eq(ApiTestcase::getInterfaceId, id)
                        .orderByAsc(ApiTestcase::getSortOrder)
                        .orderByDesc(ApiTestcase::getId));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("interface", apiInterface);
        result.put("testcases", testcases);
        result.put("testcaseCount", testcases.size());
        return Result.ok(result);
    }

    @PostMapping
    @SaCheckPermission("api:manage")
    public Result<Void> save(@PathVariable Long projectId, @RequestBody ApiInterface apiInterface) {
        apiInterface.setProjectId(projectId);
        apiInterfaceService.save(apiInterface);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("api:manage")
    public Result<Void> update(@PathVariable Long projectId, @RequestBody ApiInterface apiInterface) {
        apiInterface.setProjectId(projectId);
        apiInterfaceService.updateById(apiInterface);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("api:manage")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long projectId, @PathVariable Long id) {
        apiTestcaseService.remove(new LambdaQueryWrapper<ApiTestcase>()
                .eq(ApiTestcase::getProjectId, projectId)
                .eq(ApiTestcase::getInterfaceId, id));
        apiInterfaceService.removeById(id);
        return Result.ok();
    }
}
