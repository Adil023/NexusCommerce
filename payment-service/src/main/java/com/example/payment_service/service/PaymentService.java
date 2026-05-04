package com.example.payment_service.service;

import com.example.payment_service.dto.PaymentResponseDTO;
public interface PaymentService {
    PaymentResponseDTO makePayment(Long userId);
    void refundPayment(Long paymentId);
}