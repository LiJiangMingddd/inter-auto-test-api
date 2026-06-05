package com.lm.interautotestapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lm.interautotestapi.entity.SysPermission;
import com.lm.interautotestapi.mapper.SysPermissionMapper;
import com.lm.interautotestapi.service.SysPermissionService;
import org.springframework.stereotype.Service;

@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {
}
