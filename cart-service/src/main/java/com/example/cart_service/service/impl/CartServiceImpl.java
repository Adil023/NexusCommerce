package com.example.cart_service.service.impl;

import com.example.cart_service.client.ProductFeignClient;
import com.example.cart_service.client.UserFeignClient;
import com.example.cart_service.dto.ProductResponseDTO;
import com.example.cart_service.dto.UserResponseDTO;
import com.example.cart_service.exception.InvalidCartQuantityException;
import com.example.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ProductFeignClient productFeignClient;
    private final UserFeignClient userFeignClient;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void addToCart(Long userId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new InvalidCartQuantityException("quantity must be greater than 0");
        }

        UserResponseDTO user = userFeignClient.getUser(userId);
        log.info("User found: {}", user.getUserName());

        ProductResponseDTO product = productFeignClient.getProduct(productId);
        log.info("Product found: {}", product.getName());

        if (product.getStockCount() < quantity) {
            throw new RuntimeException("Unsufficient product stock");
        }

        String key = "cart:user:" + userId;
        String productKey = "product:" + productId;

        Object existing = redisTemplate.opsForHash().get(key, productKey);
        int currentQty = existing != null ? (Integer) existing : 0;
        int newQty = currentQty + quantity;

        if (product.getStockCount() < newQty) {
            throw new RuntimeException("Unsufficient product stock.");
        }

        redisTemplate.opsForHash().put(key, productKey, newQty);
        redisTemplate.expire(key, Duration.ofDays(1));

        log.info("Added to cart: userId={}, productId={}, quantity={}, total={}",
                userId, productId, quantity, newQty);
    }

    @Override
    public Map<Object, Object> getCartItems(Long userId) {
        String key = "cart:user:" + userId;
        return redisTemplate.opsForHash().entries(key);
    }

    @Override
    public void clearCart(Long userId) {
        String key = "cart:user:" + userId;
        redisTemplate.delete(key);
    }
}
