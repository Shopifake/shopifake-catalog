package com.shopifake.microservice.controllers;

import com.shopifake.microservice.dtos.CreateFilterRequest;
import com.shopifake.microservice.dtos.FilterResponse;
import com.shopifake.microservice.services.FilterService;
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
 * Filter endpoints.
 */
@RestController
@RequestMapping("/api/catalog/filters")
@RequiredArgsConstructor
@Tag(name = "Catalog Filters")
public class FilterController {

    private final FilterService filterService;

    @PostMapping
    @Operation(summary = "Create filter")
    public ResponseEntity<FilterResponse> createFilter(
            @Valid @RequestBody final CreateFilterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(filterService.createFilter(request));
    }

    @GetMapping
    @Operation(summary = "List filters")
    public ResponseEntity<List<FilterResponse>> listFilters(
            @RequestParam(required = false) final UUID siteId) {
        return ResponseEntity.ok(filterService.getFilters(siteId));
    }

    @DeleteMapping("/{filterId}")
    @Operation(summary = "Delete filter (if unused)")
    public ResponseEntity<Void> deleteFilter(
            @PathVariable final UUID filterId) {
        filterService.deleteFilter(filterId);
        return ResponseEntity.noContent().build();
    }
}

