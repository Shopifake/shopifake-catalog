package com.shopifake.microservice.repositories;

import com.shopifake.microservice.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for catalog categories.
 */
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findBySiteId(UUID siteId);

    Optional<Category> findBySiteIdAndNameIgnoreCase(UUID siteId, String name);

    boolean existsBySiteIdAndNameIgnoreCase(UUID siteId, String name);
}


