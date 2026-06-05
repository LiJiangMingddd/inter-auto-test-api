package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.SysUser;
import com.lm.interautotestapi.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @GetMapping("/page")
    @SaCheckPermission("user:manage")
    public Result<Page<SysUser>> page(@RequestParam(defaultValue = "1") int pageNum,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      @RequestParam(required = false) String keyword) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword);
        }
        wrapper.orderByDesc(SysUser::getId);
        return Result.ok(sysUserService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("user:manage")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.ok(sysUserService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("user:manage")
    public Result<Void> save(@RequestBody SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(SecureUtil.md5(user.getPassword()));
        }
        if (user.getAppKey() != null && !user.getAppKey().isEmpty()) {
            user.setAppKey(SecureUtil.md5(user.getAppKey()));
        }
        sysUserService.save(user);
        return Result.ok();
    }

    @PutMapping
    @SaCheckPermission("user:manage")
    public Result<Void> update(@RequestBody SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(SecureUtil.md5(user.getPassword()));
        }
        if (user.getAppKey() != null && !user.getAppKey().isEmpty()) {
            user.setAppKey(SecureUtil.md5(user.getAppKey()));
        }
        sysUserService.updateById(user);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("user:manage")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.ok();
    }
}
