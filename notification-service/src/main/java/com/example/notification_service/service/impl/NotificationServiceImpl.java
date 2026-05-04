package com.example.notification_service.service.impl;

import com.example.notification_service.dto.PaymentNotificationEvent;
import com.example.notification_service.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.email}")
    private String fromEmail;

    @Override
    @RetryableTopic(attempts = "4",
            backoff = @Backoff(delay = 2000),
            autoCreateTopics = "true",
            exclude = {NullPointerException.class, IllegalArgumentException.class}
    )
    @KafkaListener(topics = "refund-topic-v2", groupId = "payment-group")
    public void handleRefundNotification(String message) throws JsonProcessingException {
        PaymentNotificationEvent event = objectMapper.readValue(message, PaymentNotificationEvent.class);
        log.info("Kafka refund notification received. paymentId={}", event.getPaymentId());

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(event.getUserEmail());
        mailMessage.setSubject("Refund Confirmation - Payment #" + event.getPaymentId());
        mailMessage.setText(event.getMessage() +
                " Refunded Amount: " + event.getAmount() + " AZN");
        mailSender.send(mailMessage);
        log.info("Refund email sent successfully. to={}", event.getUserEmail());
    }

    @Override
    @DltHandler
    public void handleDLT(String message,
                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                          @Header(KafkaHeaders.ORIGINAL_TOPIC) String topicOriginal) {
        log.error("DLQ alert: message moved to dead letter topic. receivedTopic={}, originalTopic={}, payload={}",
                topic, topicOriginal, message);
    }
}
