package com.example.user_service.service;

import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.LoginResponseDTO;
public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
}