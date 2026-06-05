package com.lm.interautotestapi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lm.interautotestapi.entity.ApiTestcase;
import com.lm.interautotestapi.mapper.ApiTestcaseMapper;
import com.lm.interautotestapi.service.ApiTestcaseService;
import org.springframework.stereotype.Service;

@Service
public class ApiTestcaseServiceImpl extends ServiceImpl<ApiTestcaseMapper, ApiTestcase> implements ApiTestcaseService {
}
