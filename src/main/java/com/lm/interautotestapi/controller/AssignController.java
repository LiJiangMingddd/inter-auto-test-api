package com.lm.interautotestapi.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lm.interautotestapi.common.Result;
import com.lm.interautotestapi.entity.SysRolePermission;
import com.lm.interautotestapi.entity.SysUserRole;
import com.lm.interautotestapi.service.SysRolePermissionService;
import com.lm.interautotestapi.service.SysUserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assign")
@RequiredArgsConstructor
public class AssignController {

    private final SysUserRoleService sysUserRoleService;
    private final SysRolePermissionService sysRolePermissionService;

    @GetMapping("/user/{userId}/roles")
    @SaCheckPermission("user:manage")
    public Result<List<Long>> getUserRoles(@PathVariable Long userId) {
        List<Long> roleIds = sysUserRoleService.list(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return Result.ok(roleIds);
    }

    @PostMapping("/user/{userId}/roles")
    @SaCheckPermission("user:manage")
    public Result<Void> assignUserRoles(@PathVariable Long userId, @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        sysUserRoleService.remove(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            List<SysUserRole> list = new ArrayList<>();
            for (Long roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                list.add(ur);
            }
            sysUserRoleService.saveBatch(list);
        }
        return Result.ok();
    }

    @GetMapping("/role/{roleId}/permissions")
    @SaCheckPermission("role:manage")
    public Result<List<Long>> getRolePermissions(@PathVariable Long roleId) {
        List<Long> permIds = sysRolePermissionService.list(
                new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId))
                .stream().map(SysRolePermission::getPermId).collect(Collectors.toList());
        return Result.ok(permIds);
    }

    @PostMapping("/role/{roleId}/permissions")
    @SaCheckPermission("role:manage")
    public Result<Void> assignRolePermissions(@PathVariable Long roleId, @RequestBody Map<String, List<Long>> body) {
        List<Long> permIds = body.get("permIds");
        sysRolePermissionService.remove(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        if (permIds != null && !permIds.isEmpty()) {
            List<SysRolePermission> list = new ArrayList<>();
            for (Long permId : permIds) {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(roleId);
                rp.setPermId(permId);
                list.add(rp);
            }
            sysRolePermissionService.saveBatch(list);
        }
        return Result.ok();
    }
}
