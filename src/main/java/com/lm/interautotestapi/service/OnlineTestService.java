package com.lm.interautotestapi.service;

import com.lm.interautotestapi.model.OnlineTestRequest;
import com.lm.interautotestapi.model.OnlineTestResponse;

public interface OnlineTestService {

    OnlineTestResponse executeTest(OnlineTestRequest request);
}
