package com.shopifake.microservice.controllers;

import com.shopifake.microservice.dtos.CreateProductRequest;
import com.shopifake.microservice.dtos.ProductResponse;
import com.shopifake.microservice.dtos.UpdateProductRequest;
import com.shopifake.microservice.dtos.UpdateProductStatusRequest;
import com.shopifake.microservice.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST API for catalog products.
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Catalog Products")
public class ProductController {

    private final ProductService productService;

    /**
     * Create a new product.
     *
     * @param request the create product request
     * @return the created product response
     */
    @PostMapping
    @Operation(summary = "Create product")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody final CreateProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /**
     * Get a product by id.
     *
     * @param productId the product id
     * @return the product response
     */
    @GetMapping("/{productId}")
    @Operation(summary = "Get product by id")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable final UUID productId) {
        return ResponseEntity.ok(productService.getProduct(productId));
    }

    /**
     * List products.
     *
     * @param siteId the site id
     * @param status the status
     * @return the list of products
     */
    @GetMapping
    @Operation(summary = "List products")
    public ResponseEntity<List<ProductResponse>> listProducts(
            @RequestParam(required = false) final UUID siteId,
            @RequestParam(required = false) final String status) {
        return ResponseEntity.ok(productService.listProducts(siteId, status));
    }

    /**
     * List published products.
     *
     * @param siteId the site id
     * @return the list of published products
     */
    @GetMapping("/public")
    @Operation(summary = "Public storefront products")
    public ResponseEntity<List<ProductResponse>> listPublishedProducts(
            @RequestParam(required = false) final UUID siteId) {
        return ResponseEntity.ok(productService.listPublishedProducts(siteId));
    }

    /**
     * Update a product.
     *
     * @param productId the product id
     * @param request the update product request
     * @return the updated product response
     */
    @PatchMapping("/{productId}")
    @Operation(summary = "Update product metadata")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable final UUID productId,
            @Valid @RequestBody final UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(productId, request));
    }

    /**
     * Update a product status.
     *
     * @param productId the product id
     * @param request the update product status request
     * @return the updated product response
     */
    @PatchMapping("/{productId}/status")
    @Operation(summary = "Update product status")
    public ResponseEntity<ProductResponse> updateProductStatus(
            @PathVariable final UUID productId,
            @Valid @RequestBody final UpdateProductStatusRequest request) {
        return ResponseEntity.ok(productService.updateStatus(productId, request));
    }

    /**
     * Delete a product.
     *
     * @param productId the product id
     * @return no content response
     */
    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable final UUID productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}


