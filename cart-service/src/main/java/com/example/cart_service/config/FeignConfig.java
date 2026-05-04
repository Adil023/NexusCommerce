package com.example.cart_service.config;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class FeignConfig {

    private static final String INTERNAL_TRAFFIC_HEADER = "X-Internal-Traffic-Secret";
    @Value("${internal-traffic.secret:demo-secure-key-2026}")
    private String internalTrafficSecret;


    @Bean
    public RequestInterceptor internalTrafficSecretInterceptor() {
        return requestTemplate -> requestTemplate.header(INTERNAL_TRAFFIC_HEADER, internalTrafficSecret);
    }
}