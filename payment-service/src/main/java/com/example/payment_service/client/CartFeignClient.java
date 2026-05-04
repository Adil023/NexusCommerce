package com.example.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@FeignClient(name = "cart-service", fallback = CartFeignClientFallback.class)
public interface CartFeignClient {

    @GetMapping("/v1/cart/{userId}")
    Map<Object, Object> getCart(@PathVariable("userId") Long userId);

    @DeleteMapping("/v1/cart/{userId}")
    void clearCart(@PathVariable("userId") Long userId);
}
