package com.example.payment_service.service.impl;

import com.example.payment_service.client.CartFeignClient;
import com.example.payment_service.client.ProductFeignClient;
import com.example.payment_service.client.UserFeignClient;
import com.example.payment_service.dto.PaymentCompletedEvent;
import com.example.payment_service.dto.PaymentNotificationEvent;
import com.example.payment_service.dto.PaymentResponseDTO;
import com.example.payment_service.dto.ProductResponseDTO;
import com.example.payment_service.dto.UserResponseDTO;
import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.enums.PaymentStatus;
import com.example.payment_service.exception.PaymentNotFoundException;
import com.example.payment_service.mapper.PaymentEventMapper;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.util.UUID.randomUUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final PaymentEventMapper paymentEventMapper;
    private final ProductFeignClient productFeignClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final CartFeignClient cartFeignClient;
    private final UserFeignClient userFeignClient;



    @Override
    @Transactional
    public PaymentResponseDTO makePayment(Long userId) {
        Map<Object, Object> entries = cartFeignClient.getCart(userId);

        if (entries.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, Integer> cartItemsForDB = new HashMap<>();
        Map<Long, Integer> cartItemsForKafka = new HashMap<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            Long productId = Long.valueOf(entry.getKey().toString().replace("product:", ""));
            Integer quantity = (Integer) entry.getValue();

            ProductResponseDTO product = productFeignClient.getProduct(productId);
            BigDecimal itemTotal = product.getProductPrice().multiply(new BigDecimal(quantity));
            totalAmount = totalAmount.add(itemTotal);

            cartItemsForDB.put(productId + ":" + product.getName(), quantity);
            cartItemsForKafka.put(productId, quantity);
        }

        PaymentEntity paymentEntity = paymentMapper.maptoPaymentEntity(userId, totalAmount);
        paymentEntity.setStatus(PaymentStatus.PENDING);
        paymentEntity.setCartItems(cartItemsForDB);
        PaymentEntity save = paymentRepository.save(paymentEntity);

        try {
            String eventId = randomUUID().toString();
            PaymentCompletedEvent event = paymentEventMapper.toPaymentCompletedEvent(save, eventId, cartItemsForKafka);
            String message = objectMapper.writeValueAsString(event);

            kafkaTemplate.send("payment-completed-topic", save.getId().toString(), message);
            log.info("PENDING message was sent to KAFKA. Payment ID: {}", save.getId());

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Kafka could not create message", e);
        }

        cartFeignClient.clearCart(userId);

        log.info("Payment operation is PENDING and sent to Kafka");
        return paymentMapper.entityToDTO(save);
    }

    @Override
    @Transactional
    public void refundPayment(Long paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Payment is not completed");
        }

        Map<String, Integer> cartItems = payment.getCartItems();
        for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
            Long productId = Long.valueOf(entry.getKey().split(":")[0]);
            Integer productQuantity = entry.getValue();
            productFeignClient.increaseStock(productId, productQuantity);
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        log.info("Payment is refunded. paymentId={}", paymentId);

        try {
            UserResponseDTO user = userFeignClient.getUser(payment.getUserId());

            String refundMessage = "Refund Successful. Amount: " + payment.getAmount() + " AZN returned.";
            PaymentNotificationEvent event = paymentEventMapper.toPaymentNotificationEvent(
                    payment,
                    user.getEmail(),
                    refundMessage,
                    PaymentStatus.REFUNDED
            );
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("refund-topic-v2", String.valueOf(paymentId), message);
            log.info("Refund notification sent to Kafka. paymentId={}", paymentId);

        } catch (JsonProcessingException e) {
            log.error("Kafka refund notification failed. paymentId={}", paymentId, e);
        }
    }
}