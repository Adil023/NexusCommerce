package com.example.user_service.mapper;

import com.example.user_service.dto.PaginatedResponse;
import com.example.user_service.dto.UserRequestDTO;
import com.example.user_service.dto.UserResponseDTO;
import com.example.user_service.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    List<UserEntity> dtoToEntity(List<UserRequestDTO> userRequestDTO);
    UserResponseDTO entityToDTO(UserEntity userEntity);

    @Mapping(source = "content", target = "content")
    @Mapping(source = "totalPages", target = "totalPages")
    @Mapping(source = "totalElements", target = "totalElements")
    @Mapping(source = "number", target = "pageNumber")
    @Mapping(source = "size", target = "pageSize")
    PaginatedResponse<UserResponseDTO> dtoToEntityPaginatedResponse(Page<UserEntity> userEntityPage);
}