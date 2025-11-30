package com.shopifake.microservice.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Incoming payload representing the assignment of a value to a filter for a product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterAssignmentRequest {

    @NotNull(message = "filterId is required")
    private UUID filterId;

    @Size(max = 255, message = "textValue must not exceed 255 characters")
    private String textValue;

    private BigDecimal numericValue;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    private LocalDateTime startAt;

    private LocalDateTime endAt;
}

