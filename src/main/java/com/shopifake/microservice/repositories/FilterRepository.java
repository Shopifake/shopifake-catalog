package com.shopifake.microservice.repositories;

import com.shopifake.microservice.entities.Filter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for catalog filters.
 */
public interface FilterRepository extends JpaRepository<Filter, UUID> {

    List<Filter> findBySiteId(UUID siteId);

    boolean existsBySiteIdAndKeyIgnoreCase(UUID siteId, String key);

    Optional<Filter> findBySiteIdAndKeyIgnoreCase(UUID siteId, String key);
}

