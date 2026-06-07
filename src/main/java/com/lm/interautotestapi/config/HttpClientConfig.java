package com.lm.interautotestapi.config;

import cn.hutool.http.HttpGlobalConfig;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class HttpClientConfig {

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        HttpGlobalConfig.setTimeout(30000);
        HttpGlobalConfig.setMaxRedirectCount(3);
    }
}
