package com.example.payment_service.client;

import com.example.payment_service.dto.UserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/v1/users/{id}")
    UserResponseDTO getUser(@PathVariable Long id);
}
