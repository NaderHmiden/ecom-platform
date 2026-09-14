package com.platform.ecom.supplier.api;

import com.platform.ecom.supplier.domain.SupplierImportJob;
import java.time.Instant;
import java.util.UUID;

public record ImportJobResponse(
        UUID jobId,
        String sourceFileName,
        String status,
        int totalRows,
        int succeededRows,
        int failedRows,
        Instant createdAt,
        Instant completedAt
) {
    public static ImportJobResponse from(SupplierImportJob job) {
        return new ImportJobResponse(
                job.getId(),
                job.getSourceFileName(),
                job.getStatus().name(),
                job.getTotalRows(),
                job.getSucceededRows(),
                job.getFailedRows(),
                job.getCreatedAt(),
                job.getCompletedAt()
        );
    }
}