package com.platform.ecom.supplier.application;

import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.supplier.domain.RawSupplierRow;
import com.platform.ecom.supplier.domain.RowStatus;
import com.platform.ecom.supplier.infrastructure.RawSupplierRowRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Runs on AsyncConfig's dedicated thread pool — a DIFFERENT thread than
 * whatever handled the upload HTTP request. TenantContext is a
 * ThreadLocal, so it is NOT automatically visible here; the request
 * thread's JwtAuthenticationFilter never touched this thread. We set it
 * explicitly at the start and clear it at the end, the same discipline as
 * that filter, because this background job is effectively "a request"
 * from the tenant-isolation mechanism's point of view.
 */
@Service
public class SupplierRowProcessor {

    private final RawSupplierRowRepository rowRepository;
    private final SupplierRowTransactionOps transactionOps;
    private final ApplicationEventPublisher eventPublisher;

    public SupplierRowProcessor(RawSupplierRowRepository rowRepository,
                                SupplierRowTransactionOps transactionOps,
                                ApplicationEventPublisher eventPublisher) {
        this.rowRepository = rowRepository;
        this.transactionOps = transactionOps;
        this.eventPublisher = eventPublisher;
    }

    @Async("supplierImportExecutor")
    public void processJob(UUID tenantId, UUID jobId) {
        TenantContext.set(TenantId.of(tenantId));
        try {
            List<RawSupplierRow> pendingRows = rowRepository.findByImportJobIdAndStatus(jobId, RowStatus.PENDING);
            for (RawSupplierRow row : pendingRows) {
                // Each call below goes through transactionOps' real proxy
                // (different bean) — each row gets its own transaction, so
                // one bad row failing doesn't roll back rows already
                // processed successfully.
                transactionOps.processRow(row);
            }
            transactionOps.finalizeJob(jobId);
            eventPublisher.publishEvent(new SupplierImportJobCompletedEvent(tenantId, jobId));
        } finally {
            TenantContext.clear();
        }
    }
}