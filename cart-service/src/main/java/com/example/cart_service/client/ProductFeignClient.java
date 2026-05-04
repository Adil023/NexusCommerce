package com.example.cart_service.client;

import com.example.cart_service.dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductFeignClient {

    @GetMapping("/v1/product/{id}")
    ProductResponseDTO getProduct(@PathVariable Long id);
}