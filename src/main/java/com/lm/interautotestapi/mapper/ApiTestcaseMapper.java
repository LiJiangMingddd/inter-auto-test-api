package com.lm.interautotestapi.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lm.interautotestapi.entity.ApiTestcase;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiTestcaseMapper extends BaseMapper<ApiTestcase> {
}
