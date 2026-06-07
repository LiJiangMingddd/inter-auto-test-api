package com.lm.interautotestapi.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lm.interautotestapi.common.PasswordUtil;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.SysUser;
import com.lm.interautotestapi.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

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

        if (!verifyPassword(password, user.getPassword())) {
            return Result.fail("密码错误");
        }

        upgradePasswordIfNeeded(user, password);

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

    @PostMapping("/changePassword")
    public Result<Void> changePassword(@RequestBody Map<String, String> body) {
        if (!StpUtil.isLogin()) {
            return Result.fail(401, "用户未登录");
        }

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.fail(400, "旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.fail(400, "新密码不能为空");
        }
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return Result.fail(400, "确认密码不能为空");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.fail(400, "两次输入的新密码不一致");
        }
        if (oldPassword.equals(newPassword)) {
            return Result.fail(400, "新密码不能与旧密码相同");
        }

        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        if (!verifyPassword(oldPassword, user.getPassword())) {
            return Result.fail(400, "旧密码错误");
        }

        user.setPassword(PasswordUtil.encode(newPassword));
        sysUserService.updateById(user);

        return Result.ok();
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

    private boolean verifyPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return PasswordUtil.matches(rawPassword, storedPassword);
        }
        String md5Password = SecureUtil.md5(rawPassword);
        return md5Password.equals(storedPassword);
    }

    private void upgradePasswordIfNeeded(SysUser user, String rawPassword) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
            user.setPassword(PasswordUtil.encode(rawPassword));
            sysUserService.updateById(user);
        }
    }
}
