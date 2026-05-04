package com.example.payment_service.mapper;

import com.example.payment_service.dto.PaymentResponseDTO;
import com.example.payment_service.entity.PaymentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "userId", target = "userId")
    PaymentResponseDTO entityToDTO(PaymentEntity paymentEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "total", target = "amount")
    @Mapping(constant = "COMPLETED", target = "status")
    PaymentEntity maptoPaymentEntity(Long userId, BigDecimal total);
}