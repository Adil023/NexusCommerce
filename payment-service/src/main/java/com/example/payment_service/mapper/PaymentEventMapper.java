package com.example.payment_service.mapper;

import com.example.payment_service.dto.PaymentCompletedEvent;
import com.example.payment_service.dto.PaymentNotificationEvent;
import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.enums.PaymentStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface PaymentEventMapper {

    @Mapping(source = "eventId", target = "eventId")
    @Mapping(source = "payment.id", target = "paymentId")
    @Mapping(source = "payment.userId", target = "userId")
    @Mapping(source = "cartItems", target = "cartItems")
    @Mapping(source = "payment.amount", target = "totalAmount")
    PaymentCompletedEvent toPaymentCompletedEvent(PaymentEntity payment, String eventId, Map<Long, Integer> cartItems); // String → Long

    @Mapping(source = "payment.id", target = "paymentId")
    @Mapping(source = "payment.userId", target = "userId")
    @Mapping(source = "payment.amount", target = "amount")
    @Mapping(source = "message", target = "message")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "userEmail", target = "userEmail")
    PaymentNotificationEvent toPaymentNotificationEvent(PaymentEntity payment,
                                                        String userEmail,
                                                        String message,
                                                        PaymentStatus status);
}