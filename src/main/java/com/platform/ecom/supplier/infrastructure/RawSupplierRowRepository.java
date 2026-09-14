package com.platform.ecom.supplier.infrastructure;

import com.platform.ecom.supplier.domain.RawSupplierRow;
import com.platform.ecom.supplier.domain.RowStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RawSupplierRowRepository extends JpaRepository<RawSupplierRow, UUID> {

    List<RawSupplierRow> findByImportJobIdAndStatus(UUID importJobId, RowStatus status);

    long countByImportJobId(UUID importJobId);
}