package com.example.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDTO {
    String name;
    String description;
    BigDecimal productPrice;
    Integer stockCount;
    String color;
    Integer size;
}
