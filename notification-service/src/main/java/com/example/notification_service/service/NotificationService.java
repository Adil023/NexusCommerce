package com.example.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
public interface NotificationService {
    void handleRefundNotification(String message) throws JsonProcessingException;

    void handleDLT(String message, String topic, String topicOriginal);
}