package com.lm.interautotestapi.service;

import com.lm.interautotestapi.model.DifyRequest;
import com.lm.interautotestapi.model.DifyResponse;

public interface DifyService {

    DifyResponse executeWorkflow(DifyRequest request);
}
