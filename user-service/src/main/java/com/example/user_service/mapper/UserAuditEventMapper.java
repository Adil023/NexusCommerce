package com.example.user_service.mapper;

import com.example.user_service.dto.AuditEventDTO;
import com.example.user_service.enums.EventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface UserAuditEventMapper {

    AuditEventDTO toAuditEvent(Long userId, String eventId, EventType eventType, LocalDateTime eventTime);
}
