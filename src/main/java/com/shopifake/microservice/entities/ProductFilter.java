package com.shopifake.microservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a product filter attribute with foreign key to Filter.
 */
@Entity
@Table(name = "product_filters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilter {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "filter_id", nullable = false)
    private Filter filter;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

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

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}


