package com.example.user_service.service.impl;

import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.LoginResponseDTO;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.exception.InvalidCredentialsException;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.AuthService;
import com.example.user_service.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        return LoginResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiration)
                .build();
    }
}
