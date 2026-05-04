package com.example.cart_service.service;

import java.util.Map;
public interface CartService {
    void addToCart(Long userId, Long productId, Integer quantity);
    Map<Object, Object> getCartItems(Long userId);
    void clearCart(Long userId);
}