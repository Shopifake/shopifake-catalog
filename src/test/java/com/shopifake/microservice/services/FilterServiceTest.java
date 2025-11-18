package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CreateFilterRequest;
import com.shopifake.microservice.dtos.FilterResponse;
import com.shopifake.microservice.entities.Category;
import com.shopifake.microservice.entities.Filter;
import com.shopifake.microservice.entities.FilterType;
import com.shopifake.microservice.repositories.CategoryRepository;
import com.shopifake.microservice.repositories.FilterRepository;
import com.shopifake.microservice.repositories.ProductRepository;
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
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FilterService}.
 */
@ExtendWith(MockitoExtension.class)
class FilterServiceTest {

    @Mock
    private FilterRepository filterRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private FilterService filterService;

    @Test
    @DisplayName("Should create filter when category belongs to site")
    void shouldCreateFilterWithCategory() {
        UUID siteId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .siteId(siteId)
                .name("Shoes")
                .build();

        CreateFilterRequest request = CreateFilterRequest.builder()
                .siteId(siteId)
                .categoryId(categoryId)
                .key("size")
                .type(FilterType.CATEGORICAL)
                .displayName("Size")
                .values(java.util.List.of("S", "M"))
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(filterRepository.existsBySiteIdAndCategory_IdAndKeyIgnoreCase(siteId, categoryId, "size"))
                .thenReturn(false);
        when(filterRepository.save(any(Filter.class))).thenAnswer(invocation -> {
            Filter filter = invocation.getArgument(0);
            filter.setId(UUID.randomUUID());
            return filter;
        });

        FilterResponse response = filterService.createFilter(request);

        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getCategoryName()).isEqualTo("Shoes");
        assertThat(response.getKey()).isEqualTo("size");
    }

    @Test
    @DisplayName("Should reject category outside site")
    void shouldRejectCategorySiteMismatch() {
        UUID siteId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        Category category = Category.builder()
                .id(categoryId)
                .siteId(UUID.randomUUID())
                .name("Shoes")
                .build();

        CreateFilterRequest request = CreateFilterRequest.builder()
                .siteId(siteId)
                .categoryId(categoryId)
                .key("size")
                .type(FilterType.CATEGORICAL)
                .values(java.util.List.of("S"))
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> filterService.createFilter(request));

        assertThat(exception.getMessage()).contains("Category does not belong to site");
    }
}

