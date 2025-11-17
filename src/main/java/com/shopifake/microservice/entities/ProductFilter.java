package com.shopifake.microservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Embeddable representation of a product filter attribute.
 */
@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilter {

    @Column(name = "filter_key", nullable = false, length = 100)
    private String key;

    @Enumerated(EnumType.STRING)
    @Column(name = "filter_type", nullable = false, length = 25)
    private FilterType type;

    @Column(name = "text_value", length = 255)
    private String textValue;

    @Column(name = "numeric_value", precision = 19, scale = 2)
    private BigDecimal numericValue;

    @Column(name = "min_value", precision = 19, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 19, scale = 2)
    private BigDecimal maxValue;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "unit", length = 25)
    private String unit;
}


