package com.example.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDTO {
    private String token;
    private String tokenType;
    private long expiresIn;
}