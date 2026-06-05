package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.ApiInterface;
import com.lm.interautotestapi.entity.ApiTestcase;
import com.lm.interautotestapi.service.ApiInterfaceService;
import com.lm.interautotestapi.service.ApiTestcaseService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/api/interface")
public class ApiInterfaceController {

    @Resource
    private ApiInterfaceService apiInterfaceService;

    @Resource
    private ApiTestcaseService apiTestcaseService;

    @GetMapping("/page")
    @SaCheckPermission("api:manage")
    public Result<Page<ApiInterface>> page(@RequestParam(defaultValue = "1") int pageNum,
                                           @RequestParam(defaultValue = "10") int pageSize,
                                           @RequestParam(required = false) String keyword) {
        Page<ApiInterface> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ApiInterface> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(ApiInterface::getApiName, keyword)
                    .or().like(ApiInterface::getApiInfo, keyword);
        }
        wrapper.orderByDesc(ApiInterface::getId);
        return Result.ok(apiInterfaceService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("api:manage")
    public Result<ApiInterface> getById(@PathVariable Long id) {
        return Result.ok(apiInterfaceService.getById(id));
    }

    /**
     * 接口详情：包含关联的测试用例列表
     */
    @GetMapping("/{id}/detail")
    @SaCheckPermission("api:manage")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        ApiInterface apiInterface = apiInterfaceService.getById(id);
        if (apiInterface == null) {
            return Result.fail("接口不存在");
        }
        List<ApiTestcase> testcases = apiTestcaseService.list(
                new LambdaQueryWrapper<ApiTestcase>()
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
    public Result<Void> save(@RequestBody ApiInterface apiInterface) {
        apiInterfaceService.save(apiInterface);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("api:manage")
    public Result<Void> update(@RequestBody ApiInterface apiInterface) {
        apiInterfaceService.updateById(apiInterface);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("api:manage")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> delete(@PathVariable Long id) {
        // 级联删除关联的测试用例
        apiTestcaseService.remove(new LambdaQueryWrapper<ApiTestcase>().eq(ApiTestcase::getInterfaceId, id));
        apiInterfaceService.removeById(id);
        return Result.ok();
    }
}
