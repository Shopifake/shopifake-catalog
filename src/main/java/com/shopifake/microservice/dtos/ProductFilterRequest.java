package com.shopifake.microservice.dtos;

import com.shopifake.microservice.entities.FilterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Incoming payload representing a product filter definition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    @NotBlank(message = "Filter key is required")
    @Size(max = 100, message = "Filter key must not exceed 100 characters")
    private String key;

    @NotNull(message = "Filter type is required")
    private FilterType type;

    @Size(max = 255, message = "Text value must not exceed 255 characters")
    private String textValue;

    private BigDecimal numericValue;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Size(max = 25, message = "Unit must not exceed 25 characters")
    private String unit;
}


