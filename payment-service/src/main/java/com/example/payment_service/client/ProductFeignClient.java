package com.example.payment_service.client;

import com.example.payment_service.dto.ProductResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "product-service")
public interface ProductFeignClient {
    @GetMapping("/v1/product/{id}")
    ProductResponseDTO getProduct(@PathVariable Long id);

    @PutMapping("/v1/product/{id}/decrease-stock")
    void decreaseStock(@PathVariable Long id, @RequestParam Integer quantity);

    @PutMapping("/v1/product/{id}/increase-stock")
    void increaseStock(@PathVariable Long id, @RequestParam Integer quantity);
}
