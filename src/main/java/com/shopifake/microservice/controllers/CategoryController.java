package com.shopifake.microservice.controllers;

import com.shopifake.microservice.dtos.CategoryResponse;
import com.shopifake.microservice.dtos.CreateCategoryRequest;
import com.shopifake.microservice.services.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Category endpoints.
 */
@RestController
@RequestMapping("/products/categories")
@RequiredArgsConstructor
@Tag(name = "Catalog Categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create category")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody final CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @GetMapping
    @Operation(summary = "List categories")
    public ResponseEntity<List<CategoryResponse>> listCategories(
            @RequestParam(required = false) final UUID siteId) {
        return ResponseEntity.ok(categoryService.getCategories(siteId));
    }

    @DeleteMapping("/{categoryId}")
    @Operation(summary = "Delete category (if unused)")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable final UUID categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}


