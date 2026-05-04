package com.example.api_gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenApiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ecommerce Microservices API Gateway")
                        .version("v1")
                        .description("Centralized Swagger UI for downstream microservices"));
    }

}
