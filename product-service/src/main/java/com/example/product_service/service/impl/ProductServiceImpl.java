package com.example.product_service.service.impl;

import com.example.product_service.dto.PaginatedResponse;
import com.example.product_service.dto.ProductAuditEventDTO;
import com.example.product_service.dto.ProductRequestDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.entity.ProductEntity;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.exception.UnsufficientProductException;
import com.example.product_service.mapper.ProductAuditEventMapper;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static com.example.product_service.enums.EventType.PRODUCT_DELETE;
import static java.util.UUID.randomUUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ProductAuditEventMapper productAuditEventMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Cacheable(value = "products", key = "#id")
    public ProductEntity getProductId(Long id, Long userId) {
        log.info("Reading product from database. productId={}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        if (userId != null) {
            saveToHistory(userId, id);
        }
        return product;
    }

    @Override
    public void saveToHistory(Long userId, Long productId) {
        String key = "history:id:" + userId;
        redisTemplate.opsForList().leftPush(key, productId.toString());
        redisTemplate.opsForList().trim(key, 0, 49);
        redisTemplate.expire(key, Duration.ofDays(30));
        log.info("Browsing history updated. userId={}, productId={}", userId, productId);
    }

    @Override
    public void createProduct(List<ProductRequestDTO> request) {
        List<ProductEntity> productEntity = productMapper.dtoToEntity(request);
        productRepository.saveAll(productEntity);
    }

    @Override
    @Transactional("transactionManager")
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info("Deleting product. productId={}", id);
        try {
            String eventId = randomUUID().toString();
            if (productRepository.existsById(id)) {
                productRepository.deleteById(id);
                ProductAuditEventDTO dto = productAuditEventMapper.toProductAuditEvent(
                        id,
                        eventId,
                        PRODUCT_DELETE,
                        LocalDateTime.now()
                );
                String jsonMessage = objectMapper.writeValueAsString(dto);
                kafkaTemplate.send("product-audit-log-v2", jsonMessage);
                log.info("Product delete audit event sent successfully. productId={}, eventId={}", id, eventId);
            } else {
                throw new ProductNotFoundException("Product not found");
            }

        } catch (ProductNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaginatedResponse<ProductResponseDTO> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort;
        if ("asc".equalsIgnoreCase(sortDir)) {
            sort = Sort.by(sortBy).ascending();
        } else {
            sort = Sort.by(sortBy).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductEntity> productPage = productRepository.findAll(pageable);
        return productMapper.entityToPaginatedResponse(productPage);
    }

    @Override
    @Transactional
    public void decreaseStockSafe(Long id, Integer quantity) {
        int updatedRows = productRepository.decreaseStockSafe(id, quantity);
        if (updatedRows == 0) {
            throw new UnsufficientProductException("product not found and unsufficient: ID=" + id);
        }
    }

    @Override
    @Transactional
    public void increaseStockSafe(Long id, Integer quantity) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        product.setStockCount(product.getStockCount() + quantity);
        productRepository.save(product);
    }

    @Override
    public List<String> getBrowsingHistory(Long userId) {
        String key = "history:id:" + userId;
        return redisTemplate.opsForList().range(key, 0, 49);
    }


}
