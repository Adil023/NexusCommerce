package com.example.user_service.controller;

import com.example.user_service.dto.PaginatedResponse;
import com.example.user_service.dto.UserRequestDTO;
import com.example.user_service.dto.UserResponseDTO;
import com.example.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody List<@Valid UserRequestDTO> userRequestDTO) {
        userService.createUser(userRequestDTO);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserByID(@PathVariable Long id) {
        return userService.getUserByID(id);
    }

    @GetMapping
    public PaginatedResponse<UserResponseDTO> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(defaultValue = "id") String sortBy,
                                                     @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return userService.getAllUsers(page, size, sortBy, sortDir);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}