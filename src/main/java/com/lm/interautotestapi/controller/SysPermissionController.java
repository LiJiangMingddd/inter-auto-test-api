package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.SysPermission;
import com.lm.interautotestapi.service.SysPermissionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/permission")
public class SysPermissionController {

    @Resource
    private SysPermissionService sysPermissionService;

    @GetMapping("/page")
    @SaCheckPermission("perm:manage")
    public Result<Page<SysPermission>> page(@RequestParam(defaultValue = "1") int pageNum,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String keyword) {
        Page<SysPermission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysPermission::getPermName, keyword)
                    .or().like(SysPermission::getPermCode, keyword);
        }
        wrapper.orderByDesc(SysPermission::getId);
        return Result.ok(sysPermissionService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("perm:manage")
    public Result<SysPermission> getById(@PathVariable Long id) {
        return Result.ok(sysPermissionService.getById(id));
    }

    @GetMapping("/all")
    @SaCheckPermission("perm:manage")
    public Result<java.util.List<SysPermission>> all() {
        return Result.ok(sysPermissionService.list());
    }

    @PostMapping
    @SaCheckPermission("perm:manage")
    public Result<Void> save(@RequestBody SysPermission sysPermission) {
        sysPermissionService.save(sysPermission);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("perm:manage")
    public Result<Void> update(@RequestBody SysPermission sysPermission) {
        sysPermissionService.updateById(sysPermission);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("perm:manage")
    public Result<Void> delete(@PathVariable Long id) {
        sysPermissionService.removeById(id);
        return Result.ok();
    }
}
