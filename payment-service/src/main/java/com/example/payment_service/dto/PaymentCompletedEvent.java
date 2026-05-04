package com.example.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private String eventId;
    private Long paymentId;
    private Long userId;
    private Map<Long, Integer> cartItems;
    private BigDecimal totalAmount;
}