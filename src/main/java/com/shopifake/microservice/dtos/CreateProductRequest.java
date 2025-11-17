package com.shopifake.microservice.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request body used to create a new product in the catalog.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotNull(message = "siteId is required")
    private UUID siteId;

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotEmpty(message = "At least one image is required")
    private List<
            @NotBlank(message = "Image URL cannot be blank")
            @Size(max = 2048, message = "Image URL is too long")
            String> images;

    @NotEmpty(message = "At least one category is required")
    private List<@NotNull(message = "categoryId cannot be null") UUID> categoryIds;

    @Size(max = 20, message = "SKU must not exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "SKU must be alphanumeric with dashes or underscores")
    @NotBlank(message = "SKU is required")
    private String sku;

    @Builder.Default
    @NotBlank(message = "Status is required")
    private String status = "DRAFT";

    private LocalDateTime scheduledPublishAt;

    @Valid
    @Builder.Default
    private List<ProductFilterRequest> filters = List.of();
}


