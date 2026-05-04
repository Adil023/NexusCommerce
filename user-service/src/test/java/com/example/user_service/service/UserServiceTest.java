package com.example.user_service.service;

import com.example.user_service.dto.UserRequestDTO;
import com.example.user_service.dto.UserResponseDTO;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void givenValidUsersWhenCreateUserThenEncodePasswordAndSaveUsers() {

        UserRequestDTO requestDTO = new UserRequestDTO("adil", "adil@mail.com", "rawPass", "0501234567");
        UserEntity userEntity = new UserEntity();
        userEntity.setPassword("rawPass");

        when(userMapper.dtoToEntity(any())).thenReturn(List.of(userEntity));
        when(passwordEncoder.encode("rawPass")).thenReturn("hashedPass");


        userService.createUser(List.of(requestDTO));

        assertEquals("hashedPass", userEntity.getPassword());


        verify(userMapper, times(1)).dtoToEntity(any());
        verify(passwordEncoder, times(1)).encode("rawPass");
        verify(userRepository, times(1)).saveAll(any());
    }

    @Test
    void givenExistingUserWhenGetUserByIdThenReturnMappedUserResponse() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUserName("adil");

        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setUserName("adil");
        responseDTO.setEmail("adil@mail.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.entityToDTO(userEntity)).thenReturn(responseDTO);

        UserResponseDTO result = userService.getUserByID(1L);

        assertNotNull(result);
        assertEquals("adil", result.getUserName());

        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).entityToDTO(userEntity);
    }
}
