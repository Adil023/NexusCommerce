package com.example.cart_service.controller;

import com.example.cart_service.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/cart")
@RequiredArgsConstructor
public class CartController {


    private final CartService cartService;


    @PostMapping("/add/{userId}/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToCart(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam Integer quantity
    ) {

        cartService.addToCart(userId, productId, quantity);
    }

    @GetMapping("/{userId}")
    public Map<Object, Object> getCart(@PathVariable Long userId) {
        return cartService.getCartItems(userId);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
    }

}