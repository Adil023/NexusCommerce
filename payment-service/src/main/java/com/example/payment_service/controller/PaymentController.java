package com.example.payment_service.controller;

import com.example.payment_service.dto.PaymentResponseDTO;
import com.example.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import com.example.payment_service.exception.ForbiddenException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{userId}")
    public ResponseEntity<PaymentResponseDTO> pay(@PathVariable Long userId,
                                                  @RequestHeader("X-User-Id") String headerUserId) {
        if (!userId.toString().equals(headerUserId)) {
            throw new ForbiddenException("Forbidden");
        }
        PaymentResponseDTO payment = paymentService.makePayment(userId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/refund/{paymentId}")
    public void refundPayment(@PathVariable Long paymentId
                              ) {
        paymentService.refundPayment(paymentId);
    }
}
