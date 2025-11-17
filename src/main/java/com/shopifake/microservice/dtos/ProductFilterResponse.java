package com.shopifake.microservice.dtos;

import com.shopifake.microservice.entities.FilterType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO returned to clients describing a filter configured on a product.
 */
@Value
@Builder
public class ProductFilterResponse {

    String key;

    FilterType type;

    String textValue;

    BigDecimal numericValue;

    BigDecimal minValue;

    BigDecimal maxValue;

    LocalDateTime startAt;

    LocalDateTime endAt;

    String unit;
}


