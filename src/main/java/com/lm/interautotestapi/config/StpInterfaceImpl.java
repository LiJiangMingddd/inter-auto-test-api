package com.lm.interautotestapi.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lm.interautotestapi.entity.*;
import com.lm.interautotestapi.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysUserRoleService sysUserRoleService;
    private final SysRolePermissionService sysRolePermissionService;
    private final SysRoleService sysRoleService;
    private final SysPermissionService sysPermissionService;

    @Override
    @Cacheable(value = "permissions", key = "#loginId", unless = "#result.isEmpty()")
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<Long> roleIds = sysUserRoleService.list(
                        new LambdaQueryWrapper<SysUserRole>()
                                .select(SysUserRole::getRoleId)
                                .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Long> permIds = sysRolePermissionService.list(
                        new LambdaQueryWrapper<SysRolePermission>()
                                .select(SysRolePermission::getPermId)
                                .in(SysRolePermission::getRoleId, roleIds))
                .stream().map(SysRolePermission::getPermId).collect(Collectors.toSet());
        if (permIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysPermissionService.list(
                        new LambdaQueryWrapper<SysPermission>()
                                .select(SysPermission::getPermCode)
                                .in(SysPermission::getId, permIds))
                .stream().map(SysPermission::getPermCode).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "roles", key = "#loginId", unless = "#result.isEmpty()")
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<Long> roleIds = sysUserRoleService.list(
                        new LambdaQueryWrapper<SysUserRole>()
                                .select(SysUserRole::getRoleId)
                                .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysRoleService.list(
                        new LambdaQueryWrapper<SysRole>()
                                .select(SysRole::getRoleCode)
                                .in(SysRole::getId, roleIds))
                .stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }
}
