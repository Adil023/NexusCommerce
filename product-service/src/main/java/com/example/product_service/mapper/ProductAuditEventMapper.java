package com.example.product_service.mapper;

import com.example.product_service.dto.ProductAuditEventDTO;
import com.example.product_service.enums.EventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface ProductAuditEventMapper {

    ProductAuditEventDTO toProductAuditEvent(Long id, String eventId, EventType eventType, LocalDateTime eventTime);
}
