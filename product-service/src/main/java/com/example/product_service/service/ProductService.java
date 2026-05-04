package com.example.product_service.service;

import com.example.product_service.dto.PaginatedResponse;
import com.example.product_service.dto.ProductRequestDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.entity.ProductEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
public interface ProductService {
    ProductEntity getProductId(Long id, Long userId);
    void saveToHistory(Long userId, Long productId);
    void createProduct(List<ProductRequestDTO> request);
    void deleteProduct(Long id);
    PaginatedResponse<ProductResponseDTO> getAllProducts(int page, int size, String sortBy, String sortDir);
    void decreaseStockSafe(Long id, Integer quantity);
    void increaseStockSafe(Long id, Integer quantity);
    List<String> getBrowsingHistory(Long userId);
}