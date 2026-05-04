package com.example.user_service.service;

import com.example.user_service.dto.PaginatedResponse;
import com.example.user_service.dto.UserRequestDTO;
import com.example.user_service.dto.UserResponseDTO;
import java.util.List;

public interface UserService {
    void createUser(List<UserRequestDTO> userRequestDTO);
    UserResponseDTO getUserByID(Long id);
    void deleteUser(Long id);
    PaginatedResponse<UserResponseDTO> getAllUsers(int page, int size, String sortBy, String sortDir);
}
