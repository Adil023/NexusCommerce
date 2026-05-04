package com.example.payment_service.service.impl;

import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.enums.PaymentStatus;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentConsumerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentConsumerServiceImpl implements PaymentConsumerService {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Override
    @KafkaListener(topics = "stock-decreased-topic", groupId = "payment-complete-group")
    public void handleStockDecreased(String message) {
        if (message == null || message.isBlank()) {
            log.warn("Received empty Kafka message for topic stock-decreased-topic");
            return;
        }

        try {
            JsonNode node = objectMapper.readTree(message);
            if (node == null || !node.hasNonNull("paymentId")) {
                log.warn("Invalid stock-decreased-topic payload (missing paymentId). Message={}", message);
                return;
            }

            Long paymentId = node.get("paymentId").asLong();
            if (paymentId == null) {
                log.warn("Parsed paymentId is null. Message={}", message);
                return;
            }

            PaymentEntity payment = paymentRepository.findById(paymentId).orElse(null);
            if (payment == null) {
                log.warn("Payment not found for paymentId={}. Skipping stock-decreased event.", paymentId);
                return;
            }

            payment.setStatus(PaymentStatus.COMPLETED);
            paymentRepository.save(payment);

            log.info("Payment status was made COMPLETED. Payment ID: {}", paymentId);
        } catch (Exception e) {
            // Demo safety: skip malformed/unexpected messages rather than rethrowing.
            log.error("Error when handling stock-decreased-topic. Skipping message. Message={}", message, e);
        }
    }
}
