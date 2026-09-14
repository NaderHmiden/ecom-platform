package com.platform.ecom.supplier.infrastructure;

import com.platform.ecom.supplier.domain.SupplierImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SupplierImportJobRepository extends JpaRepository<SupplierImportJob, UUID> {

    // Backs idempotent re-upload: check this BEFORE creating a new job for
    // a given file's content hash.
    Optional<SupplierImportJob> findByTenantIdAndContentHash(UUID tenantId, String contentHash);

    // Atomic increments — see SupplierImportJob's class comment for why
    // these bypass optimistic locking rather than load-mutate-save. The
    // caller must run these inside a @Transactional method.
    @Modifying
    @Query("UPDATE SupplierImportJob j SET j.succeededRows = j.succeededRows + 1 WHERE j.id = :jobId")
    void incrementSucceeded(@Param("jobId") UUID jobId);

    @Modifying
    @Query("UPDATE SupplierImportJob j SET j.failedRows = j.failedRows + 1 WHERE j.id = :jobId")
    void incrementFailed(@Param("jobId") UUID jobId);
}