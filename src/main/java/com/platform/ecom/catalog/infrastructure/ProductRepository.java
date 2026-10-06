package com.platform.ecom.catalog.infrastructure;

import com.platform.ecom.catalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    List<Product> findByTenantId(UUID tenantId);
    Optional<Product> findByIdAndTenantId(UUID id, UUID tenantId);

    // Backs CatalogImportService's idempotency check: "did this row
    // already produce a Product?"
    Optional<Product> findByTenantIdAndSourceRowId(UUID tenantId, UUID sourceRowId);
}