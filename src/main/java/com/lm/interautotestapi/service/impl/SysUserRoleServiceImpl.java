package com.lm.interautotestapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lm.interautotestapi.entity.SysUserRole;
import com.lm.interautotestapi.mapper.SysUserRoleMapper;
import com.lm.interautotestapi.service.SysUserRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {
}
