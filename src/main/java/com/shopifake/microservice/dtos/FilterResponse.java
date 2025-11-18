package com.shopifake.microservice.dtos;

import com.shopifake.microservice.entities.FilterType;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO representing a filter.
 */
@Value
@Builder
public class FilterResponse {

    UUID id;
    UUID siteId;
    String key;
    FilterType type;
    String displayName;
    String unit;
    List<String> values;
    BigDecimal minValue;
    BigDecimal maxValue;
    LocalDateTime createdAt;
}

