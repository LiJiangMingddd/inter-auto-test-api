package com.lm.interautotestapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Knife4j (OpenAPI 3) 接口文档配置
 * <p>
 * 基于 SpringDoc 实现，knife4j-openapi3-spring-boot-starter v4.x 底层使用 SpringDoc 而非 Springfox。
 * 在线文档：http://localhost:8849/doc.html
 */
@Configuration
public class Knife4jConfig {

    /**
     * 全局 OpenAPI 信息 + 安全认证方案
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("接口自动化中台 - Open API 文档")
                        .description("对外暴露的批量导入接口，供 Agent 或其他外部系统调用。\n\n" +
                                "认证方式：先通过 /api/auth/getToken 获取 token，再在 Authorization 头携带 token 调用接口。")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("lijiangming")
                                .email("admin@example.com")))
                // 全局安全方案（API Key 在请求头中）
                .schemaRequirement("Authorization",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization")
                                .description("通过 /api/auth/getToken 获取的 Sa-Token"))
                .security(Collections.singletonList(
                        new SecurityRequirement().addList("Authorization")));
    }

    /**
     * Open API（对外接口）分组
     */
    @Bean
    public GroupedOpenApi openApiExternalGroup() {
        return GroupedOpenApi.builder()
                .group("Open API（对外接口）")
                .displayName("Open API（对外接口）")
                .packagesToScan("com.lm.interautotestapi.controller")
                .pathsToMatch("/api/open/**")
                .build();
    }

    /**
     * 认证接口分组 — 展示 getToken 接口
     */
    @Bean
    public GroupedOpenApi authGroup() {
        return GroupedOpenApi.builder()
                .group("认证接口")
                .displayName("认证接口")
                .packagesToScan("com.lm.interautotestapi.controller")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    /**
     * 系统管理接口分组
     */
    @Bean
    public GroupedOpenApi systemGroup() {
        return GroupedOpenApi.builder()
                .group("系统管理")
                .displayName("系统管理")
                .packagesToScan("com.lm.interautotestapi.controller")
                .pathsToMatch("/api/user/**", "/api/role/**", "/api/permission/**", "/api/assign/**")
                .build();
    }

    /**
     * 业务管理接口分组
     */
    @Bean
    public GroupedOpenApi businessGroup() {
        return GroupedOpenApi.builder()
                .group("业务管理")
                .displayName("业务管理")
                .packagesToScan("com.lm.interautotestapi.controller")
                .pathsToMatch("/api/interface/**", "/api/testcase/**")
                .build();
    }
}