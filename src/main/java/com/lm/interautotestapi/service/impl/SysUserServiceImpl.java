package com.lm.interautotestapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lm.interautotestapi.entity.SysUser;
import com.lm.interautotestapi.mapper.SysUserMapper;
import com.lm.interautotestapi.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
}
