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
import java.util.List;
import java.util.UUID;

/**
 * Request payload to create a filter.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFilterRequest {

    @NotNull(message = "siteId is required")
    private UUID siteId;

    @NotNull(message = "categoryId is required")
    private UUID categoryId;

    @NotBlank(message = "key is required")
    @Size(max = 100, message = "key must not exceed 100 characters")
    private String key;

    @NotNull(message = "type is required")
    private FilterType type;

    @Size(max = 255, message = "displayName must not exceed 255 characters")
    private String displayName;

    @Size(max = 25, message = "unit must not exceed 25 characters")
    private String unit;

    @Size(max = 255, message = "Each value must not exceed 255 characters")
    private List<@Size(max = 255) String> values;

    private BigDecimal minValue;

    private BigDecimal maxValue;
}

