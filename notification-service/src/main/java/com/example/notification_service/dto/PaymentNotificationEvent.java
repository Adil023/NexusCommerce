package com.example.notification_service.dto;

import com.example.notification_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentNotificationEvent {
    private Long paymentId;
    private Long userId;
    private BigDecimal amount;
    private String message;
    private PaymentStatus status;
    private String userEmail;
}