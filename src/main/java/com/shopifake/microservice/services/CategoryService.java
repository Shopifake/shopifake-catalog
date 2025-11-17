package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CategoryResponse;
import com.shopifake.microservice.dtos.CreateCategoryRequest;
import com.shopifake.microservice.entities.Category;
import com.shopifake.microservice.repositories.CategoryRepository;
import com.shopifake.microservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for catalog categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /**
     * Create a category for a site.
     */
    @Transactional
    public CategoryResponse createCategory(final CreateCategoryRequest request) {
        if (categoryRepository.existsBySiteIdAndNameIgnoreCase(request.getSiteId(), request.getName())) {
            throw new IllegalArgumentException("Category already exists for this site: " + request.getName());
        }

        Category category = Category.builder()
                .siteId(request.getSiteId())
                .name(request.getName().trim())
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    /**
     * Retrieve categories optionally filtered by site.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(final UUID siteId) {
        List<Category> categories = siteId == null
                ? categoryRepository.findAll()
                : categoryRepository.findBySiteId(siteId);
        return categories.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Delete category if unused.
     */
    @Transactional
    public void deleteCategory(final UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
        if (productRepository.existsByCategories_Id(categoryId)) {
            throw new IllegalStateException("Category is linked to products and cannot be deleted");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse mapToResponse(final Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .siteId(category.getSiteId())
                .name(category.getName())
                .createdAt(category.getCreatedAt())
                .build();
    }
}


