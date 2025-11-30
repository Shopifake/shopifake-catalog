package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CreateFilterRequest;
import com.shopifake.microservice.dtos.FilterResponse;
import com.shopifake.microservice.entities.Category;
import com.shopifake.microservice.entities.Filter;
import com.shopifake.microservice.entities.FilterType;
import com.shopifake.microservice.repositories.CategoryRepository;
import com.shopifake.microservice.repositories.FilterRepository;
import com.shopifake.microservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service for catalog filters.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FilterService {

    private final FilterRepository filterRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a filter for a site.
     */
    @Transactional
    public FilterResponse createFilter(final CreateFilterRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + request.getCategoryId()));

        if (!category.getSiteId().equals(request.getSiteId())) {
            throw new IllegalArgumentException("Category does not belong to site: " + request.getSiteId());
        }

        if (filterRepository.existsBySiteIdAndCategory_IdAndKeyIgnoreCase(
                request.getSiteId(), category.getId(), request.getKey())) {
            throw new IllegalArgumentException("Filter already exists for this category: " + request.getKey());
        }

        validateFilterRequest(request);

        Filter filter = Filter.builder()
                .siteId(request.getSiteId())
                .category(category)
                .key(request.getKey().trim())
                .type(request.getType())
                .displayName(request.getDisplayName() != null ? request.getDisplayName().trim() : null)
                .unit(request.getUnit() != null ? request.getUnit().trim() : null)
                .values(request.getValues() != null ? new ArrayList<>(request.getValues()) : new ArrayList<>())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .build();

        return mapToResponse(filterRepository.save(filter));
    }

    /**
     * Retrieve filters optionally filtered by site.
     */
    @Transactional(readOnly = true)
    public List<FilterResponse> getFilters(final UUID siteId) {
        List<Filter> filters = siteId == null
                ? filterRepository.findAll()
                : filterRepository.findBySiteId(siteId);
        return filters.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Delete filter if unused.
     */
    @Transactional
    public void deleteFilter(final UUID filterId) {
        Filter filter = filterRepository.findById(filterId)
                .orElseThrow(() -> new IllegalArgumentException("Filter not found: " + filterId));
        if (productRepository.existsByFilters_Filter_Id(filterId)) {
            throw new IllegalStateException("Filter is linked to products and cannot be deleted");
        }
        filterRepository.delete(filter);
    }

    private void validateFilterRequest(final CreateFilterRequest request) {
        switch (request.getType()) {
            case CATEGORICAL -> {
                if (request.getValues() == null || request.getValues().isEmpty()) {
                    throw new IllegalArgumentException("Values are required for categorical filters");
                }
                if (StringUtils.hasText(request.getUnit())) {
                    throw new IllegalArgumentException("Unit is not allowed for categorical filters");
                }
                ensureNoValues(request.getMinValue(), request.getMaxValue(), "categorical");
            }
            case QUANTITATIVE -> {
                if (!StringUtils.hasText(request.getUnit())) {
                    throw new IllegalArgumentException("Unit is required for quantitative filters");
                }
                if (request.getValues() != null && !request.getValues().isEmpty()) {
                    throw new IllegalArgumentException("Values are not allowed for quantitative filters");
                }
                validateValues(request.getMinValue(), request.getMaxValue());
            }
            case DATETIME -> {
                if (StringUtils.hasText(request.getUnit())) {
                    throw new IllegalArgumentException("Unit is not allowed for datetime filters");
                }
                if (request.getValues() != null && !request.getValues().isEmpty()) {
                    throw new IllegalArgumentException("Values are not allowed for datetime filters");
                }
                ensureNoValues(request.getMinValue(), request.getMaxValue(), "datetime");
            }
            default -> throw new IllegalArgumentException("Unsupported filter type: " + request.getType());
        }
    }

    private void validateValues(final BigDecimal minValue, final BigDecimal maxValue) {
        if (minValue != null && maxValue != null && maxValue.compareTo(minValue) < 0) {
            throw new IllegalArgumentException("maxValue must be greater than or equal to minValue");
        }
    }

    private void ensureNoValues(final BigDecimal minValue, final BigDecimal maxValue, final String type) {
        if (minValue != null || maxValue != null) {
            throw new IllegalArgumentException("Values are not supported for " + type + " filters");
        }
    }

    private FilterResponse mapToResponse(final Filter filter) {
        return FilterResponse.builder()
                .id(filter.getId())
                .siteId(filter.getSiteId())
                .categoryId(filter.getCategory() != null ? filter.getCategory().getId() : null)
                .categoryName(filter.getCategory() != null ? filter.getCategory().getName() : null)
                .key(filter.getKey())
                .type(filter.getType())
                .displayName(filter.getDisplayName())
                .unit(filter.getUnit())
                .values(filter.getValues())
                .minValue(filter.getMinValue())
                .maxValue(filter.getMaxValue())
                .createdAt(filter.getCreatedAt())
                .build();
    }
}

