package com.lm.interautotestapi.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.SysUser;
import com.lm.interautotestapi.service.SysUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return Result.fail("用户名和密码不能为空");
        }
        SysUser user = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            return Result.fail("用户不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.fail("用户已被禁用");
        }

        // 密码校验：对传入密码做 MD5 后与数据库对比
        String md5Password = SecureUtil.md5(password);
        if (!md5Password.equals(user.getPassword())) {
            return Result.fail("密码错误");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        return Result.ok(result);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return Result.ok();
    }

    @PostMapping("/getToken")
    public Result<Map<String, Object>> getToken(@RequestBody Map<String, String> body) {
        String appId = body.get("appId");
        String appKey = body.get("appKey");
        if (appId == null || appKey == null) {
            return Result.fail("appId和appKey不能为空");
        }
        SysUser user = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getAppId, appId));
        if (user == null) {
            return Result.fail("appId不存在");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.fail("用户已被禁用");
        }
        String md5AppKey = SecureUtil.md5(appKey);
        if (!md5AppKey.equals(user.getAppKey())) {
            return Result.fail("appKey校验失败");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("username", user.getUsername());
        return Result.ok(result);
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info() {
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("appId", user.getAppId());
        result.put("roles", StpUtil.getRoleList());
        result.put("permissions", StpUtil.getPermissionList());
        return Result.ok(result);
    }
}
