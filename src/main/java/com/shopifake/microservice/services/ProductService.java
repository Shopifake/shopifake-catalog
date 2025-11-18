package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CategoryResponse;
import com.shopifake.microservice.dtos.CreateProductRequest;
import com.shopifake.microservice.dtos.ProductFilterAssignmentRequest;
import com.shopifake.microservice.dtos.ProductFilterResponse;
import com.shopifake.microservice.dtos.ProductResponse;
import com.shopifake.microservice.dtos.UpdateProductRequest;
import com.shopifake.microservice.dtos.UpdateProductStatusRequest;
import com.shopifake.microservice.entities.FilterType;
import com.shopifake.microservice.entities.Product;
import com.shopifake.microservice.entities.ProductFilter;
import com.shopifake.microservice.entities.ProductStatus;
import com.shopifake.microservice.entities.Category;
import com.shopifake.microservice.entities.Filter;
import com.shopifake.microservice.repositories.CategoryRepository;
import com.shopifake.microservice.repositories.FilterRepository;
import com.shopifake.microservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Business logic for catalog products.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FilterRepository filterRepository;
    private final Clock clock = Clock.systemUTC();

    /**
     * Create a new product with the provided payload.
     */
    @Transactional
    public ProductResponse createProduct(final CreateProductRequest request) {
        log.info("Creating product with SKU {}", request.getSku());
        validateSkuUniqueness(request.getSku(), null);
        validateImages(request.getImages());
        var categories = loadCategories(request.getSiteId(), request.getCategoryIds());
        List<ProductFilter> filters = mapFilters(request.getFilters(), request.getSiteId());
        ProductStatus status = parseStatus(request.getStatus());
        LocalDateTime scheduledPublishAt = validateSchedule(status, request.getScheduledPublishAt());

        Product product = Product.builder()
                .siteId(request.getSiteId())
                .name(request.getName().trim())
                .description(request.getDescription().trim())
                .images(new ArrayList<>(request.getImages()))
                .categories(categories)
                .sku(request.getSku().toUpperCase())
                .status(status)
                .scheduledPublishAt(scheduledPublishAt)
                .publishedAt(status == ProductStatus.PUBLISHED ? LocalDateTime.now(clock) : null)
                .build();
        
        // Set product reference in filters
        filters.forEach(filter -> filter.setProduct(product));
        product.setFilters(filters);

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Update an existing product.
     */
    @Transactional
    public ProductResponse updateProduct(final UUID productId, final UpdateProductRequest request) {
        Product product = getProductOrThrow(productId);

        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName().trim());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getImages() != null) {
            if (request.getImages().isEmpty()) {
                throw new IllegalArgumentException("Images list cannot be empty");
            }
            validateImages(request.getImages());
            product.setImages(new ArrayList<>(request.getImages()));
        }

        if (request.getCategoryIds() != null) {
            if (request.getCategoryIds().isEmpty()) {
                throw new IllegalArgumentException("categoryIds cannot be empty");
            }
            product.setCategories(loadCategories(product.getSiteId(), request.getCategoryIds()));
        }

        if (StringUtils.hasText(request.getSku())) {
            validateSkuUniqueness(request.getSku(), productId);
            product.setSku(request.getSku().toUpperCase());
        }

        if (request.getFilters() != null) {
            List<ProductFilter> filters = mapFilters(request.getFilters(), product.getSiteId());
            // Clear existing filters and set new ones
            product.getFilters().clear();
            filters.forEach(filter -> filter.setProduct(product));
            product.getFilters().addAll(filters);
        }

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Update the status lifecycle of a product.
     */
    @Transactional
    public ProductResponse updateStatus(final UUID productId, final UpdateProductStatusRequest request) {
        Product product = getProductOrThrow(productId);
        ProductStatus newStatus = parseStatus(request.getStatus());
        LocalDateTime scheduledPublishAt = validateSchedule(newStatus, request.getScheduledPublishAt());

        product.setStatus(newStatus);

        switch (newStatus) {
            case PUBLISHED -> {
                product.setScheduledPublishAt(null);
                product.setPublishedAt(LocalDateTime.now(clock));
            }
            case SCHEDULED -> {
                product.setScheduledPublishAt(scheduledPublishAt);
                product.setPublishedAt(null);
            }
            default -> {
                product.setScheduledPublishAt(null);
                product.setPublishedAt(null);
            }
        }

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    /**
     * Retrieve a product by id.
     */
    @Transactional(readOnly = true)
    public ProductResponse getProduct(final UUID productId) {
        return mapToResponse(getProductOrThrow(productId));
    }

    /**
     * List products optionally filtered by site and status.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts(final UUID siteId, final String status) {
        List<Product> products;
        if (siteId != null && status != null) {
            products = productRepository.findBySiteIdAndStatus(siteId, parseStatus(status));
        } else if (siteId != null) {
            products = productRepository.findBySiteId(siteId);
        } else if (status != null) {
            products = productRepository.findByStatus(parseStatus(status));
        } else {
            products = productRepository.findAll();
        }
        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Public-facing list of published products for a site.
     */
    @Transactional(readOnly = true)
    public List<ProductResponse> listPublishedProducts(final UUID siteId) {
        List<Product> products;
        if (siteId == null) {
            products = productRepository.findByStatus(ProductStatus.PUBLISHED);
        } else {
            products = productRepository.findBySiteIdAndStatus(siteId, ProductStatus.PUBLISHED);
        }
        return products.stream().map(this::mapToResponse).toList();
    }

    /**
     * Delete a product permanently.
     */
    @Transactional
    public void deleteProduct(final UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found with id " + productId);
        }
        productRepository.deleteById(productId);
    }

    private Product getProductOrThrow(final UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id " + productId));
    }

    private void validateSkuUniqueness(final String sku, final UUID currentId) {
        productRepository.findBySku(sku.toUpperCase())
                .ifPresent(existing -> {
                    if (currentId == null || !existing.getId().equals(currentId)) {
                        throw new IllegalArgumentException("SKU already exists: " + sku);
                    }
                });
    }

    private void validateImages(final List<String> images) {
        for (String image : images) {
            if (!StringUtils.hasText(image)) {
                throw new IllegalArgumentException("Image URL cannot be blank");
            }
            try {
                URI uri = new URI(image);
                if (uri.getScheme() == null || (!"http".equalsIgnoreCase(uri.getScheme())
                        && !"https".equalsIgnoreCase(uri.getScheme()))) {
                    throw new IllegalArgumentException("Image URL must be http or https: " + image);
                }
            } catch (URISyntaxException ex) {
                throw new IllegalArgumentException("Invalid image URL: " + image, ex);
            }
        }
    }

    private ProductStatus parseStatus(final String status) {
        try {
            return ProductStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid product status: " + status);
        }
    }

    private LocalDateTime validateSchedule(final ProductStatus status, final LocalDateTime requestedTime) {
        if (status == ProductStatus.SCHEDULED) {
            if (requestedTime == null) {
                throw new IllegalArgumentException("scheduledPublishAt is required for scheduled products");
            }
            if (!requestedTime.isAfter(LocalDateTime.now(clock))) {
                throw new IllegalArgumentException("scheduledPublishAt must be in the future");
            }
            return requestedTime;
        }
        if (requestedTime != null) {
            throw new IllegalArgumentException("scheduledPublishAt is only allowed for scheduled products");
        }
        return null;
    }

    private List<ProductFilter> mapFilters(final List<ProductFilterAssignmentRequest> filterRequests, final UUID siteId) {
        if (filterRequests == null || filterRequests.isEmpty()) {
            return List.of();
        }
        return filterRequests.stream()
                .map(request -> mapFilter(request, siteId))
                .toList();
    }

    private ProductFilter mapFilter(final ProductFilterAssignmentRequest request, final UUID siteId) {
        Filter filter = filterRepository.findById(request.getFilterId())
                .orElseThrow(() -> new IllegalArgumentException("Filter not found: " + request.getFilterId()));
        if (!filter.getSiteId().equals(siteId)) {
            throw new IllegalArgumentException("Filter " + filter.getKey() + " does not belong to site " + siteId);
        }

        validateFilterPayload(request, filter);

        return ProductFilter.builder()
                .filter(filter)
                .textValue(request.getTextValue())
                .numericValue(request.getNumericValue())
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();
    }

    private void validateFilterPayload(final ProductFilterAssignmentRequest request, final Filter filter) {
        FilterType type = filter.getType();
        String filterLabel = "filter " + filter.getKey();

        switch (type) {
            case CATEGORICAL -> {
                if (!StringUtils.hasText(request.getTextValue())) {
                    throw new IllegalArgumentException("textValue is required for " + filterLabel);
                }
                if (filter.getValues() != null && !filter.getValues().isEmpty()
                        && !filter.getValues().contains(request.getTextValue())) {
                    throw new IllegalArgumentException("textValue must match one of the allowed values for " + filterLabel);
                }
                ensureNull(request.getNumericValue(), "numericValue", filterLabel);
                ensureNull(request.getMinValue(), "minValue", filterLabel);
                ensureNull(request.getMaxValue(), "maxValue", filterLabel);
                ensureNull(request.getStartAt(), "startAt", filterLabel);
                ensureNull(request.getEndAt(), "endAt", filterLabel);
            }
            case QUANTITATIVE -> {
                if (StringUtils.hasText(request.getTextValue())) {
                    throw new IllegalArgumentException("textValue is not allowed for " + filterLabel);
                }
                if (request.getNumericValue() == null
                        && (request.getMinValue() == null || request.getMaxValue() == null)) {
                    throw new IllegalArgumentException(
                            "Provide numericValue or min/max range for " + filterLabel);
                }
                validateNumericRange(request.getMinValue(), request.getMaxValue());
                validateValuesAgainstDefinition(request, filter, filterLabel);
                ensureNull(request.getStartAt(), "startAt", filterLabel);
                ensureNull(request.getEndAt(), "endAt", filterLabel);
            }
            case DATETIME -> {
                if (request.getStartAt() == null) {
                    throw new IllegalArgumentException("startAt is required for " + filterLabel);
                }
                if (request.getEndAt() != null && request.getEndAt().isBefore(request.getStartAt())) {
                    throw new IllegalArgumentException("endAt must be after startAt for " + filterLabel);
                }
                ensureNull(request.getTextValue(), "textValue", filterLabel);
                ensureNull(request.getNumericValue(), "numericValue", filterLabel);
                ensureNull(request.getMinValue(), "minValue", filterLabel);
                ensureNull(request.getMaxValue(), "maxValue", filterLabel);
            }
            default -> throw new IllegalArgumentException("Unsupported filter type " + type);
        }
    }

    private void validateValuesAgainstDefinition(final ProductFilterAssignmentRequest request,
                                                 final Filter filter,
                                                 final String filterLabel) {
        BigDecimal minValue = filter.getMinValue();
        BigDecimal maxValue = filter.getMaxValue();

        if (minValue != null) {
            if (request.getNumericValue() != null && request.getNumericValue().compareTo(minValue) < 0) {
                throw new IllegalArgumentException("numericValue must be >= " + minValue + " for " + filterLabel);
            }
            if (request.getMinValue() != null && request.getMinValue().compareTo(minValue) < 0) {
                throw new IllegalArgumentException("minValue must be >= " + minValue + " for " + filterLabel);
            }
        }
        if (maxValue != null) {
            if (request.getNumericValue() != null && request.getNumericValue().compareTo(maxValue) > 0) {
                throw new IllegalArgumentException("numericValue must be <= " + maxValue + " for " + filterLabel);
            }
            if (request.getMaxValue() != null && request.getMaxValue().compareTo(maxValue) > 0) {
                throw new IllegalArgumentException("maxValue must be <= " + maxValue + " for " + filterLabel);
            }
        }
    }

    private void ensureNull(final Object value, final String fieldName, final String filterLabel) {
        if (value != null) {
            throw new IllegalArgumentException(fieldName + " is not supported for " + filterLabel);
        }
    }

    private void validateNumericRange(final BigDecimal min, final BigDecimal max) {
        if (min != null && max != null && max.compareTo(min) < 0) {
            throw new IllegalArgumentException("maxValue must be greater than or equal to minValue");
        }
    }


    private List<ProductFilterResponse> mapFilterResponses(final List<ProductFilter> filters) {
        if (filters == null) {
            return List.of();
        }
        return filters.stream()
                .map(pf -> {
                    Filter filter = pf.getFilter();
                    return ProductFilterResponse.builder()
                            .filterId(filter.getId())
                            .key(filter.getKey())
                            .type(filter.getType())
                            .displayName(filter.getDisplayName())
                            .textValue(pf.getTextValue())
                            .numericValue(pf.getNumericValue())
                            .startAt(pf.getStartAt())
                            .endAt(pf.getEndAt())
                            .unit(filter.getUnit())
                            .build();
                })
                .toList();
    }

    private ProductResponse mapToResponse(final Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .siteId(product.getSiteId())
                .name(product.getName())
                .description(product.getDescription())
                .images(List.copyOf(product.getImages()))
                .categories(mapCategoryResponses(product.getCategories()))
                .sku(product.getSku())
                .status(product.getStatus())
                .scheduledPublishAt(product.getScheduledPublishAt())
                .publishedAt(product.getPublishedAt())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .filters(mapFilterResponses(product.getFilters()))
                .build();
    }

    private List<CategoryResponse> mapCategoryResponses(final Set<Category> categories) {
        if (categories == null) {
            return List.of();
        }
        return categories.stream()
                .map(this::mapCategory)
                .toList();
    }

    private CategoryResponse mapCategory(final Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .siteId(category.getSiteId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .build();
    }

    private Set<Category> loadCategories(final UUID siteId, final List<UUID> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException("categoryIds are required");
        }
        Set<UUID> uniqueIds = new HashSet<>(categoryIds);
        List<Category> categories = categoryRepository.findAllById(uniqueIds);
        if (categories.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("One or more categories do not exist");
        }
        boolean mismatchedSite = categories.stream()
                .anyMatch(category -> !category.getSiteId().equals(siteId));
        if (mismatchedSite) {
            throw new IllegalArgumentException("Categories must belong to the same site as the product");
        }
        return new HashSet<>(categories);
    }
}


