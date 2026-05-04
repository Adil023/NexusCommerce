package com.example.user_service.dto;


import com.example.user_service.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditEventDTO {
    private Long userId;
    private String eventId;
    private EventType eventType;
    private LocalDateTime eventTime;
}
