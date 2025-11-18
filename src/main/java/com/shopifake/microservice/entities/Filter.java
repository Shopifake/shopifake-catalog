package com.shopifake.microservice.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Filter entity scoped per site.
 * Represents available filters that can be used for products.
 */
@Entity
@Table(name = "filters")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "site_id", nullable = false)
    private UUID siteId;

    @Column(name = "filter_key", nullable = false, length = 100)
    private String key;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "filter_type", nullable = false, length = 25)
    private FilterType type;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "unit", length = 25)
    private String unit;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "filter_values", joinColumns = @JoinColumn(name = "filter_id"))
    @Column(name = "value_text", nullable = false, length = 255)
    @Builder.Default
    private List<String> values = new ArrayList<>();

    @Column(name = "min_value", precision = 19, scale = 2)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 19, scale = 2)
    private BigDecimal maxValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
    }
}

