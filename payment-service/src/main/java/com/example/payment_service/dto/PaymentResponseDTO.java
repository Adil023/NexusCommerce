package com.example.payment_service.dto;

import com.example.payment_service.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private Long id;
    private BigDecimal amount;
    private PaymentStatus status;
    private Long userId;

}