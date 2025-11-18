package com.shopifake.microservice.dtos;

import com.shopifake.microservice.entities.FilterType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO returned to clients describing a filter configured on a product.
 */
@Value
@Builder
public class ProductFilterResponse {

    UUID filterId;
    String key;
    UUID categoryId;
    String categoryName;

    FilterType type;

    String displayName;

    String textValue;

    BigDecimal numericValue;

    BigDecimal minValue;

    BigDecimal maxValue;

    LocalDateTime startAt;

    LocalDateTime endAt;

    String unit;

    List<String> values;
}


