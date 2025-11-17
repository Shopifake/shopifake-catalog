package com.shopifake.microservice.repositories;

import com.shopifake.microservice.entities.Product;
import com.shopifake.microservice.entities.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for catalog products.
 */
public interface ProductRepository extends JpaRepository<Product, UUID> {

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    List<Product> findByStatus(ProductStatus status);

    List<Product> findBySiteId(UUID siteId);

    List<Product> findBySiteIdAndStatus(UUID siteId, ProductStatus status);

    List<Product> findByStatusAndScheduledPublishAtBefore(ProductStatus status, LocalDateTime before);
}


