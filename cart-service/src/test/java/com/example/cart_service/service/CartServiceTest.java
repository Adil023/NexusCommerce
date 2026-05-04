package com.example.cart_service.service;

import com.example.cart_service.client.ProductFeignClient;
import com.example.cart_service.client.UserFeignClient;
import com.example.cart_service.dto.ProductResponseDTO;
import com.example.cart_service.dto.UserResponseDTO;
import com.example.cart_service.service.impl.CartServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductFeignClient productFeignClient;
    @Mock
    private UserFeignClient userFeignClient;
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void givenValidCartDataWhenAddToCartThenStoreQuantityInRedis() {
        UserResponseDTO user = new UserResponseDTO();
        user.setUserName("john");

        ProductResponseDTO product = new ProductResponseDTO();
        product.setName("Phone");
        product.setProductPrice(BigDecimal.valueOf(100));
        product.setStockCount(10);

        when(userFeignClient.getUser(1L)).thenReturn(user);
        when(productFeignClient.getProduct(2L)).thenReturn(product);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.get("cart:user:1", "2")).thenReturn(1);

        // 2. Action
        cartService.addToCart(1L, 2L, 2);

        // 3. Assertions
        assertNotNull(user);
        assertEquals("john", user.getUserName());

        // 4. Verifications
        verify(userFeignClient, times(1)).getUser(1L);
        verify(productFeignClient, times(1)).getProduct(2L);
        verify(hashOperations, times(1)).put("cart:user:1", "2", 3);
        verify(redisTemplate, times(1)).expire(any(String.class), any());
    }

    @Test
    void givenCartExistsWhenGetCartItemsThenReturnRedisEntries() {
        // 1. Inline Mocks (Stubbing)
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("cart:user:1")).thenReturn(Map.of("2", 3));

        // 2. Action
        Map<Object, Object> result = cartService.getCartItems(1L);

        // 3. Assertions
        assertNotNull(result);
        assertEquals(3, result.get("2"));

        // 4. Verifications
        verify(redisTemplate, times(1)).opsForHash();
        verify(hashOperations, times(1)).entries("cart:user:1");
    }
}
