package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lm.interautotestapi.common.PasswordUtil;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.SysUser;
import com.lm.interautotestapi.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

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

    /**
     * 公开的用户搜索接口（仅需登录），用于前端选择用户
     * 只返回基本用户信息，不包含敏感字段
     */
    @GetMapping("/search")
    @SaCheckLogin
    public Result<List<Map<String, Object>>> search(@RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "50") int limit) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword);
        }
        wrapper.orderByDesc(SysUser::getId).last("LIMIT " + limit);
        List<SysUser> users = sysUserService.list(wrapper);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("nickname", u.getNickname());
            m.put("email", u.getEmail());
            return m;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    @SaCheckPermission("user:manage")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.ok(sysUserService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("user:manage")
    public Result<Map<String, String>> save(@RequestBody SysUser user) {
        if (user.getAppId() == null || user.getAppId().isEmpty()) {
            user.setAppId("APP_" + IdUtil.simpleUUID().toUpperCase());
        }
        String rawAppKey = null;
        if (user.getAppKey() == null || user.getAppKey().isEmpty()) {
            rawAppKey = IdUtil.simpleUUID().toUpperCase() + IdUtil.fastSimpleUUID().toUpperCase();
            user.setAppKey(SecureUtil.md5(rawAppKey));
        } else {
            user.setAppKey(SecureUtil.md5(user.getAppKey()));
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }
        sysUserService.save(user);

        Map<String, String> result = new HashMap<>();
        result.put("id", user.getId().toString());
        result.put("appId", user.getAppId());
        if (rawAppKey != null) {
            result.put("appKey", rawAppKey);
        }
        return Result.ok(result);
    }

    @PutMapping
    @SaCheckPermission("user:manage")
    public Result<Void> update(@RequestBody SysUser user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
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
