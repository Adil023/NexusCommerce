package com.example.user_service.service.impl;

import com.example.user_service.dto.AuditEventDTO;
import com.example.user_service.dto.PaginatedResponse;
import com.example.user_service.dto.UserRequestDTO;
import com.example.user_service.dto.UserResponseDTO;
import com.example.user_service.entity.UserEntity;
import com.example.user_service.enums.EventType;
import com.example.user_service.exception.UserNotFoundException;
import com.example.user_service.mapper.UserAuditEventMapper;
import com.example.user_service.mapper.UserMapper;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.UUID.randomUUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserAuditEventMapper userAuditEventMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void createUser(List<UserRequestDTO> userRequestDTO) {
        List<UserEntity> userEntities = userMapper.dtoToEntity(userRequestDTO);

        for (UserEntity user : userEntities) {
            String hashedPassword = passwordEncoder.encode(user.getPassword());
            user.setPassword(hashedPassword);
        }
        userRepository.saveAll(userEntities);
    }

    @Override
    public UserResponseDTO getUserByID(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userMapper.entityToDTO(user);
    }

    @Override
    @Transactional("transactionManager")
    public void deleteUser(Long id) {
        String eventId = randomUUID().toString();

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        userRepository.delete(user);
        AuditEventDTO event = userAuditEventMapper.toAuditEvent(
                id,
                eventId,
                EventType.DELETE_USER,
                LocalDateTime.now()
        );

        try {
            String jsonMessage = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("user-audit-log-v2", jsonMessage);
            log.info("User delete audit event sent successfully. userId={}, eventId={}", id, eventId);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for Kafka message", e);
        }
    }

    @Override
    public PaginatedResponse<UserResponseDTO> getAllUsers(int page, int size, String sortBy, String sortDir) {
        Sort sort;
        if ("asc".equalsIgnoreCase(sortDir)) {
            sort = Sort.by(sortBy).ascending();
        } else {
            sort = Sort.by(sortBy).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserEntity> userPage = userRepository.findAll(pageable);
        return userMapper.dtoToEntityPaginatedResponse(userPage);
    }
}
