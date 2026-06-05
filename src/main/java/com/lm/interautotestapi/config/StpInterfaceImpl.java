package com.lm.interautotestapi.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lm.interautotestapi.entity.*;
import com.lm.interautotestapi.service.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private SysUserRoleService sysUserRoleService;

    @Resource
    private SysRolePermissionService sysRolePermissionService;

    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysPermissionService sysPermissionService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<Long> roleIds = sysUserRoleService.list(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> permIds = sysRolePermissionService.list(
                        new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds))
                .stream().map(SysRolePermission::getPermId).distinct().collect(Collectors.toList());
        if (permIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sysPermissionService.listByIds(permIds)
                .stream().map(SysPermission::getPermCode).collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        List<Long> roleIds = sysUserRoleService.list(
                        new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }
        return sysRoleService.listByIds(roleIds)
                .stream().map(SysRole::getRoleCode).collect(Collectors.toList());
    }
}
