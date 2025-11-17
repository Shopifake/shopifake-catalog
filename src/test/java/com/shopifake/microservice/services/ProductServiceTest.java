package com.shopifake.microservice.services;

import com.shopifake.microservice.dtos.CreateProductRequest;
import com.shopifake.microservice.dtos.UpdateProductStatusRequest;
import com.shopifake.microservice.entities.Product;
import com.shopifake.microservice.entities.ProductStatus;
import com.shopifake.microservice.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ProductService}.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private CreateProductRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = CreateProductRequest.builder()
                .siteId(UUID.randomUUID())
                .name("Premium Hoodie")
                .description("Soft cotton hoodie")
                .images(List.of("https://cdn.example.com/h1.png"))
                .categories(List.of("apparel"))
                .sku("hoodie-001")
                .status("DRAFT")
                .build();
    }

    @Test
    @DisplayName("Should persist a product when payload is valid")
    void shouldCreateProduct() {
        when(productRepository.findBySku("HOODIE-001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            product.setId(UUID.randomUUID());
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            return product;
        });

        var response = productService.createProduct(validRequest);

        assertThat(response.getId()).isNotNull();
        assertEquals("HOODIE-001", response.getSku());
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertEquals(ProductStatus.DRAFT, productCaptor.getValue().getStatus());
        assertThat(CollectionUtils.isEmpty(productCaptor.getValue().getFilters())).isTrue();
    }

    @Test
    @DisplayName("Should reject duplicate SKU")
    void shouldRejectDuplicateSku() {
        Product existing = Product.builder()
                .id(UUID.randomUUID())
                .sku("HOODIE-001")
                .build();
        when(productRepository.findBySku("HOODIE-001")).thenReturn(Optional.of(existing));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(validRequest));

        assertThat(exception.getMessage()).contains("SKU already exists");
        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should enforce schedule date for scheduled products")
    void shouldRequireScheduledDate() {
        Product product = Product.builder()
                .id(UUID.randomUUID())
                .sku("HOODIE-001")
                .status(ProductStatus.DRAFT)
                .build();
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        UpdateProductStatusRequest request = UpdateProductStatusRequest.builder()
                .status("SCHEDULED")
                .build();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateStatus(product.getId(), request));

        assertThat(exception.getMessage()).contains("scheduledPublishAt is required");
    }
}


