package com.example.product_service.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    @NotEmpty(message = "Name can not be empty")
    private String name;
    @NotEmpty(message = "Description can not be empty")
    private String description;
    @NotNull(message = "Product price can not be empty")
    private BigDecimal productPrice;
    @NotNull(message = "Stock count can not be empty")
    private Integer stockCount;
    @NotEmpty(message = "Color can not be empty")
    private String color;
    @NotNull(message = "Size can not be null")
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must be at most 100")
    private Integer size;
}