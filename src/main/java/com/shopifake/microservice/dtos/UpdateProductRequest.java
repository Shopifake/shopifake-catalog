package com.shopifake.microservice.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Partial update payload for products.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    private String description;

    private List<
            @Size(max = 2048, message = "Image URL is too long")
            String> images;

    private List<UUID> categoryIds;

    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "SKU must be alphanumeric with dashes or underscores")
    @Size(max = 20, message = "SKU must not exceed 20 characters")
    private String sku;

    @Valid
    private List<ProductFilterAssignmentRequest> filters;
}


