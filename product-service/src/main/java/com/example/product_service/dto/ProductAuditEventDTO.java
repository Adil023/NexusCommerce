package com.example.product_service.dto;

import com.example.product_service.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductAuditEventDTO {
    private Long id;
    private String eventId;
    private EventType eventType;
    private LocalDateTime eventTime;
}
