package com.example.product_service.exception;

public class UnsufficientProductException extends RuntimeException {
    public UnsufficientProductException(String message) {
        super(message);
    }
}
