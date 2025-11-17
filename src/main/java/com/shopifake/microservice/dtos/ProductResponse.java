package com.shopifake.microservice.dtos;

import com.shopifake.microservice.entities.ProductStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO returned to clients for product read operations.
 */
@Value
@Builder
public class ProductResponse {

    UUID id;

    UUID siteId;

    String name;

    String description;

    List<String> images;

    List<String> categories;

    String sku;

    ProductStatus status;

    LocalDateTime scheduledPublishAt;

    LocalDateTime publishedAt;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;

    List<ProductFilterResponse> filters;
}


