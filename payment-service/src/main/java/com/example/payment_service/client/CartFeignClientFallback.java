package com.example.payment_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class CartFeignClientFallback implements CartFeignClient {

    @Override
    public Map<Object, Object> getCart(Long userId) {
        log.error("Circuit Breaker Worked. Cart Service is not answering.");
        return Collections.emptyMap();
    }

    @Override
    public void clearCart(Long userId) {
        log.error("Circuit Breaker Worked. the basket could not be clear.");
    }
}