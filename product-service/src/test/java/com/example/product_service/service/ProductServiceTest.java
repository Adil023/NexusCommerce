package com.example.product_service.service;

import com.example.product_service.dto.ProductRequestDTO;
import com.example.product_service.entity.ProductEntity;
import com.example.product_service.exception.UnsufficientProductException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.impl.ProductServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void givenValidProductsWhenCreateProductThenMapAndSaveAll() {

        ProductRequestDTO requestDTO = new ProductRequestDTO("Phone", "Good phone", BigDecimal.TEN, 5, "Black", 42);
        ProductEntity productEntity = new ProductEntity();
        productEntity.setProductName("Phone");

        when(productMapper.dtoToEntity(org.mockito.ArgumentMatchers.<List<ProductRequestDTO>>any()))
                .thenReturn(List.of(productEntity));

        productService.createProduct(List.of(requestDTO));

        Assertions.assertNotNull(productEntity);
        Assertions.assertEquals("Phone", productEntity.getProductName());

        verify(productMapper, times(1)).dtoToEntity(org.mockito.ArgumentMatchers.<List<ProductRequestDTO>>any());
        verify(productRepository, times(1)).saveAll(any());
    }

    @Test
    void givenZeroUpdatedRowsWhenDecreaseStockSafeThenThrowUnsufficientProductException() {
        when(productRepository.decreaseStockSafe(1L, 2)).thenReturn(0);

        assertThrows(UnsufficientProductException.class,
                () -> productService.decreaseStockSafe(1L, 2));

        verify(productRepository, times(1)).decreaseStockSafe(1L, 2);
    }
}
