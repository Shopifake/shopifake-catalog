package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CreateCategoryRequest;
import com.shopifake.microservice.entities.Category;
import com.shopifake.microservice.repositories.CategoryRepository;
import com.shopifake.microservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CreateCategoryRequest request;

    @BeforeEach
    void setUp() {
        request = CreateCategoryRequest.builder()
                .siteId(UUID.randomUUID())
                .name("Accessories")
                .build();
    }

    @Test
    @DisplayName("Should create category when unique per site")
    void shouldCreateCategory() {
        when(categoryRepository.existsBySiteIdAndNameIgnoreCase(request.getSiteId(), request.getName()))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            category.setCreatedAt(java.time.LocalDateTime.now());
            return category;
        });

        var response = categoryService.createCategory(request);

        assertThat(response.getId()).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Should block deletion when category has products")
    void shouldPreventDeleteWhenInUse() {
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .siteId(request.getSiteId())
                .name("Accessories")
                .build();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.existsByCategories_Id(categoryId)).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> categoryService.deleteCategory(categoryId));

        assertThat(exception.getMessage()).contains("linked to products");
        verify(categoryRepository, never()).delete(category);
    }
}


