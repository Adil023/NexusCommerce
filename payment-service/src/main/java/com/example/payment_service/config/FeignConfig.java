package com.example.payment_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    private static final String INTERNAL_TRAFFIC_HEADER = "X-Internal-Traffic-Secret";

    @Value("${internal.traffic.secret}")
    private String internalTrafficSecret;

    @Bean
    public RequestInterceptor internalTrafficHeaderInterceptor() {
        return template -> template.header(INTERNAL_TRAFFIC_HEADER, internalTrafficSecret);
    }
}
