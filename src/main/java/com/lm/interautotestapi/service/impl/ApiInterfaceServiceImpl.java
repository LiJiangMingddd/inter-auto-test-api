package com.lm.interautotestapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lm.interautotestapi.entity.ApiInterface;
import com.lm.interautotestapi.mapper.ApiInterfaceMapper;
import com.lm.interautotestapi.service.ApiInterfaceService;
import org.springframework.stereotype.Service;

@Service
public class ApiInterfaceServiceImpl extends ServiceImpl<ApiInterfaceMapper, ApiInterface> implements ApiInterfaceService {
}
