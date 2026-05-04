package com.example.payment_service.service;

import com.example.payment_service.client.CartFeignClient;
import com.example.payment_service.client.ProductFeignClient;
import com.example.payment_service.client.UserFeignClient;
import com.example.payment_service.dto.PaymentResponseDTO;
import com.example.payment_service.dto.ProductResponseDTO;
import com.example.payment_service.entity.PaymentEntity;
import com.example.payment_service.enums.PaymentStatus;
import com.example.payment_service.mapper.PaymentEventMapper;
import com.example.payment_service.mapper.PaymentMapper;
import com.example.payment_service.repository.PaymentRepository;
import com.example.payment_service.service.impl.PaymentServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private ProductFeignClient productFeignClient;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CartFeignClient cartFeignClient;
    @Mock
    private PaymentEventMapper  paymentEventMapper;
    @InjectMocks
    private PaymentServiceImpl paymentService;


    @Test
    void givenValidCartWhenMakePaymentThenReturnPaymentResponse() throws Exception {
        Map<Object, Object> cartEntries = Map.of("2", 2);
        ProductResponseDTO product = ProductResponseDTO.builder()
                .name("Phone")
                .productPrice(BigDecimal.valueOf(100))
                .stockCount(10)
                .build();

        PaymentEntity mappedEntity = new PaymentEntity();
        mappedEntity.setUserId(1L);
        mappedEntity.setAmount(BigDecimal.valueOf(200));
        mappedEntity.setStatus(PaymentStatus.COMPLETED);

        PaymentEntity savedEntity = new PaymentEntity();
        savedEntity.setId(10L);
        savedEntity.setUserId(1L);
        savedEntity.setAmount(BigDecimal.valueOf(200));
        savedEntity.setStatus(PaymentStatus.PENDING);

        PaymentResponseDTO responseDTO = new PaymentResponseDTO(10L, BigDecimal.valueOf(200), PaymentStatus.PENDING, 1L);

        when(cartFeignClient.getCart(1L)).thenReturn(cartEntries);
        when(productFeignClient.getProduct(2L)).thenReturn(product);
        when(paymentMapper.maptoPaymentEntity(1L, BigDecimal.valueOf(200))).thenReturn(mappedEntity);
        when(paymentRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(objectMapper.writeValueAsString(any())).thenReturn("kafka-message");
        when(paymentMapper.entityToDTO(savedEntity)).thenReturn(responseDTO);
        PaymentResponseDTO result = paymentService.makePayment(1L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(BigDecimal.valueOf(200), result.getAmount());

        verify(cartFeignClient, times(1)).getCart(1L);
        verify(productFeignClient, times(1)).getProduct(2L);
        verify(paymentRepository, times(1)).save(mappedEntity);
        verify(kafkaTemplate, times(1)).send(any(String.class), any(String.class), any(String.class));
        verify(cartFeignClient, times(1)).clearCart(1L);
    }

    @Test
    void givenJsonFailureWhenMakePaymentThenThrowRuntimeException() throws Exception {

        Map<Object, Object> cartEntries = Map.of("2", 1);
        ProductResponseDTO product = ProductResponseDTO.builder()
                .name("Phone")
                .productPrice(BigDecimal.valueOf(50))
                .stockCount(10)
                .build();

        PaymentEntity mappedEntity = new PaymentEntity();
        mappedEntity.setUserId(1L);
        mappedEntity.setAmount(BigDecimal.valueOf(50));
        mappedEntity.setStatus(PaymentStatus.PENDING);

        PaymentEntity savedEntity = new PaymentEntity();
        savedEntity.setId(11L);
        savedEntity.setUserId(1L);
        savedEntity.setAmount(BigDecimal.valueOf(50));
        savedEntity.setStatus(PaymentStatus.PENDING);

        when(cartFeignClient.getCart(1L)).thenReturn(cartEntries);
        when(productFeignClient.getProduct(2L)).thenReturn(product);
        when(paymentMapper.maptoPaymentEntity(1L, BigDecimal.valueOf(50))).thenReturn(mappedEntity);
        when(paymentRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("json error") {
        });

        RuntimeException exception = assertThrows(RuntimeException.class, () -> paymentService.makePayment(1L));

        assertNotNull(exception);
        assertEquals("Kafka could not create message", exception.getMessage());


        verify(cartFeignClient, times(1)).getCart(1L);
        verify(productFeignClient, times(1)).getProduct(2L);
        verify(paymentRepository, times(1)).save(mappedEntity);
    }
}
