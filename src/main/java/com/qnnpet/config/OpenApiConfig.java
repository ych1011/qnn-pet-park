package com.qnnpet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置
 * - 文档元信息
 * - JWT Bearer 鉴权方案（Swagger UI 顶部 Authorize 按钮）
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearer-token";

    @Bean
    public OpenAPI qnnPetParkOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QNN宠物乐园 API")
                        .description("小学课堂积分激励系统 - 后端接口文档")
                        .version("v1")
                        .license(new License().name("MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")
                                        .description("JWT 访问令牌，格式：Bearer {token}（登录接口返回的 token）")));
    }
}