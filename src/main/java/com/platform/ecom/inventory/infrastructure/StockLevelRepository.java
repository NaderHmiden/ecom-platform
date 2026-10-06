package com.platform.ecom.inventory.infrastructure;

import com.platform.ecom.inventory.domain.StockLevel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, UUID> {

    Optional<StockLevel> findByTenantIdAndSku(UUID tenantId, String sku);

    /**
     * Pessimistic write lock query (SELECT ... FOR UPDATE) to serialize
     * concurrent stock reservation requests on the same SKU, preventing
     * race conditions and overselling.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockLevel s WHERE s.tenantId = :tenantId AND s.sku = :sku")
    Optional<StockLevel> findByTenantIdAndSkuPessimistic(@Param("tenantId") UUID tenantId, @Param("sku") String sku);
}
