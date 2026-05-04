package com.example.product_service.service.impl;

import com.example.product_service.dto.PaymentCompletedEvent;
import com.example.product_service.entity.ProcessedEvent;
import com.example.product_service.repository.ProcessedEventRepository;
import com.example.product_service.service.ProductConsumerService;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductConsumerServiceImpl implements ProductConsumerService {

    private final ObjectMapper objectMapper;
    private final ProductService productService;
    private final ProcessedEventRepository processedEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional
    @KafkaListener(topics = "payment-completed-topic",
            groupId = "product_stock_group_v2")
    public void handlePaymentCompleted(String message) {
        if (message == null || message.isBlank()) {
            log.warn("Received empty Kafka message");
            return;
        }

        try {
            PaymentCompletedEvent event = objectMapper.readValue(message,
                    PaymentCompletedEvent.class);

            log.info("Received Payment event. Event ID: {}", event.getEventId());

            if (processedEventRepository.existsById(event.getEventId())) {
                log.warn("Duplicate message: {}", event.getEventId());
                return;
            }

            try {
                processedEventRepository.saveAndFlush(
                        new ProcessedEvent(event.getEventId(), LocalDateTime.now()));
            } catch (DataIntegrityViolationException e) {
                log.warn("Duplicate event by DB constraint. Event ID: {}",
                        event.getEventId());
                return;
            }

            event.getCartItems().forEach((productId, quantity) -> {
                productService.decreaseStockSafe(productId, quantity);
                log.info("Stock decreased. productId={}, quantity={}",
                        productId, quantity);
            });

            String confirmMessage = objectMapper.writeValueAsString(
                    Map.of("paymentId", event.getPaymentId())
            );
            kafkaTemplate.send("stock-decreased-topic",
                    event.getPaymentId().toString(), confirmMessage);

            log.info("Stock decrease confirmed. Event ID: {}", event.getEventId());

        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize. Skipping. Message={}", message, e);
        } catch (Exception e) {
            log.error("Processing failed. Rolling back. Message={}", message, e);
            throw new RuntimeException("Kafka payment event processing failed", e);
        }
    }
}
