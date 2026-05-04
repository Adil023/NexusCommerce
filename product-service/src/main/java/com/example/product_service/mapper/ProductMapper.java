package com.example.product_service.mapper;

import com.example.product_service.dto.PaginatedResponse;
import com.example.product_service.dto.ProductRequestDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "name", target = "productName")
    @Mapping(source = "description", target = "productDescription")
    ProductEntity dtoToEntity(ProductRequestDTO request);

    List<ProductEntity> dtoToEntity(List<ProductRequestDTO> request);

    @Mapping(source = "productName", target = "name")
    @Mapping(source = "productDescription", target = "description")
    ProductResponseDTO entityToDTO(ProductEntity productEntity);


    @Mapping(source = "content", target = "content")
    @Mapping(source = "totalPages", target = "totalPages")
    @Mapping(source = "totalElements", target = "totalElements")
    @Mapping(source = "number", target = "pageNumber")
    @Mapping(source = "size", target = "pageSize")
    PaginatedResponse<ProductResponseDTO> entityToPaginatedResponse(Page<ProductEntity> productEntities);
}
