package com.example.product_service.controller;

import com.example.product_service.dto.PaginatedResponse;
import com.example.product_service.dto.ProductRequestDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/v1/product")
@RequiredArgsConstructor
@Tag(
        name = "Product API",
        description = "CRUD operations for product management"
)
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Operation(summary = "Get Product by ID")
    @GetMapping("/{id}")
    public ProductResponseDTO getProductId(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return productMapper.entityToDTO(productService.getProductId(id, userId));
    }

    @Operation(summary = "Create new Product")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@RequestBody List<@Valid ProductRequestDTO> request) {
        productService.createProduct(request);
    }

    @GetMapping
    public PaginatedResponse<ProductResponseDTO> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size,
                                                                @RequestParam(defaultValue = "id") String sortBy,
                                                                @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return productService.getAllProducts(page, size, sortBy, sortDir);
    }

    @GetMapping("/history/{userId}")
    public List<String> getBrowsingHistory(@PathVariable Long userId) {
        return productService.getBrowsingHistory(userId);
    }

    @PutMapping("/{id}/decrease-stock")
    public void decreaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        productService.decreaseStockSafe(id, quantity);
    }

    @PutMapping("/{id}/increase-stock")
    public void increaseStock(@PathVariable Long id, @RequestParam Integer quantity) {
        productService.increaseStockSafe(id, quantity);
    }

    @Operation(summary = "Delete Product by ID")
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}
