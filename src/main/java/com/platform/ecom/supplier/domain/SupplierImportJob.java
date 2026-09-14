package com.platform.ecom.supplier.domain;

import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "supplier_import_jobs",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_import_jobs_tenant_content_hash",
                columnNames = {"tenant_id", "content_hash"}
        )
)
public class SupplierImportJob extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "source_file_name", nullable = false)
    private String sourceFileName;

    // SHA-256 of the uploaded file's bytes. This is the actual mechanism
    // behind "re-uploading the same file twice must not create duplicate
    // products" (Phase 0, journey J1) — the unique constraint above makes
    // a duplicate upload a database-level conflict, not something the
    // application layer has to remember to check for.
    @Column(name = "content_hash", nullable = false)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(name = "total_rows", nullable = false)
    private int totalRows;

    // Deliberately NOT read-modify-written via this entity during row
    // processing — see SupplierImportJobRepository's atomic increment
    // methods. These fields are only trustworthy read fresh from the DB,
    // or once processing has fully finished (Phase 1: job counters are
    // eventually consistent, not transactionally guaranteed).
    @Column(name = "succeeded_rows", nullable = false)
    private int succeededRows;

    @Column(name = "failed_rows", nullable = false)
    private int failedRows;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    private long version;

    protected SupplierImportJob() {
        // required by Hibernate
    }

    private SupplierImportJob(UUID id, UUID tenantId, String sourceFileName, String contentHash) {
        super(tenantId);
        this.id = id;
        this.sourceFileName = sourceFileName;
        this.contentHash = contentHash;
        this.status = JobStatus.PENDING;
        this.totalRows = 0;
        this.succeededRows = 0;
        this.failedRows = 0;
        this.createdAt = Instant.now();
    }

    public static SupplierImportJob create(UUID tenantId, String sourceFileName, String contentHash) {
        if (sourceFileName == null || sourceFileName.isBlank()) {
            throw new InvariantViolationException("Import job must reference a source file name");
        }
        if (contentHash == null || contentHash.isBlank()) {
            throw new InvariantViolationException("Import job must have a content hash");
        }
        return new SupplierImportJob(UUID.randomUUID(), tenantId, sourceFileName, contentHash);
    }

    public void beginProcessing(int totalRows) {
        if (status != JobStatus.PENDING) {
            throw new InvariantViolationException("Only a PENDING job can begin processing, was: " + status);
        }
        this.status = JobStatus.PROCESSING;
        this.totalRows = totalRows;
    }

    /**
     * Called once, after every row has been processed, with FRESH counts
     * read from the database (not this entity's in-memory fields — those
     * may be stale, since rows update counters via direct SQL, not through
     * this object). Deriving final status from ground truth, one time, is
     * simpler and safer than trying to keep this entity's state live
     * throughout concurrent row processing.
     */
    public void finalizeWith(int freshSucceededRows, int freshFailedRows) {
        if (status != JobStatus.PROCESSING) {
            throw new InvariantViolationException("Only a PROCESSING job can be finalized, was: " + status);
        }
        this.succeededRows = freshSucceededRows;
        this.failedRows = freshFailedRows;
        if (freshSucceededRows == 0 && freshFailedRows > 0) {
            this.status = JobStatus.FAILED;
        } else if (freshFailedRows > 0) {
            this.status = JobStatus.PARTIALLY_COMPLETED;
        } else {
            this.status = JobStatus.COMPLETED;
        }
        this.completedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getSourceFileName() { return sourceFileName; }
    public String getContentHash() { return contentHash; }
    public JobStatus getStatus() { return status; }
    public int getTotalRows() { return totalRows; }
    public int getSucceededRows() { return succeededRows; }
    public int getFailedRows() { return failedRows; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}