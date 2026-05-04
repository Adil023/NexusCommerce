package com.example.product_service.repository;

import com.example.product_service.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stockCount = p.stockCount - :quantity WHERE p.id = :id AND p.stockCount >= :quantity")
    int decreaseStockSafe(@Param("id") Long id, @Param("quantity") Integer quantity);

}