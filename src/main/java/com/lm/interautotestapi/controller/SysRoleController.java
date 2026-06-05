package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.SysRole;
import com.lm.interautotestapi.service.SysRoleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/role")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;

    @GetMapping("/page")
    @SaCheckPermission("role:manage")
    public Result<Page<SysRole>> page(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(required = false) String keyword) {
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysRole::getRoleName, keyword)
                    .or().like(SysRole::getRoleCode, keyword);
        }
        wrapper.orderByDesc(SysRole::getId);
        return Result.ok(sysRoleService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("role:manage")
    public Result<SysRole> getById(@PathVariable Long id) {
        return Result.ok(sysRoleService.getById(id));
    }

    @GetMapping("/all")
    @SaCheckPermission("role:manage")
    public Result<java.util.List<SysRole>> all() {
        return Result.ok(sysRoleService.list());
    }

    @PostMapping
    @SaCheckPermission("role:manage")
    public Result<Void> save(@RequestBody SysRole sysRole) {
        sysRoleService.save(sysRole);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("role:manage")
    public Result<Void> update(@RequestBody SysRole sysRole) {
        sysRoleService.updateById(sysRole);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("role:manage")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return Result.ok();
    }
}
