package com.shopifake.microservice.dtos;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO representing a category.
 */
@Value
@Builder
public class CategoryResponse {

    UUID id;
    UUID siteId;
    String name;
    LocalDateTime createdAt;
}


