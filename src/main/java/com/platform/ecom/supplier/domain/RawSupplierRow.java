package com.platform.ecom.supplier.domain;

import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "raw_supplier_rows")
public class RawSupplierRow extends TenantScopedEntity {

    @Id
    private UUID id;

    // References the job by ID only — NOT a JPA @ManyToOne. Loading a row
    // never needs to load its parent job, and this keeps the two aggregates
    // genuinely independent (Phase 1: this split exists specifically so a
    // row update never has to touch, or lock, the job).
    @Column(name = "import_job_id", nullable = false)
    private UUID importJobId;

    @Column(name = "row_number", nullable = false)
    private int rowNumber;

    // Raw row data as JSON text. Deliberately not typed columns per
    // supplier field — Phase 6 hasn't defined the real Product schema yet,
    // and locking this table to today's guess at supplier columns would be
    // exactly the kind of premature structure Phase 5 shouldn't introduce.
    @Column(name = "raw_data_json", nullable = false, columnDefinition = "text")
    private String rawDataJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RowStatus status;

    @Column(name = "validation_error")
    private String validationError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RawSupplierRow() {
        // required by Hibernate
    }

    private RawSupplierRow(UUID id, UUID tenantId, UUID importJobId, int rowNumber, String rawDataJson) {
        super(tenantId);
        this.id = id;
        this.importJobId = importJobId;
        this.rowNumber = rowNumber;
        this.rawDataJson = rawDataJson;
        this.status = RowStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public static RawSupplierRow create(UUID tenantId, UUID importJobId, int rowNumber, String rawDataJson) {
        if (rawDataJson == null || rawDataJson.isBlank()) {
            throw new InvariantViolationException("Raw supplier row must have data");
        }
        return new RawSupplierRow(UUID.randomUUID(), tenantId, importJobId, rowNumber, rawDataJson);
    }

    public void markNormalized() {
        this.status = RowStatus.NORMALIZED;
        this.validationError = null;
    }

    public void markValidationFailed(String reason) {
        this.status = RowStatus.VALIDATION_FAILED;
        this.validationError = reason;
    }

    public UUID getId() { return id; }
    public UUID getImportJobId() { return importJobId; }
    public int getRowNumber() { return rowNumber; }
    public String getRawDataJson() { return rawDataJson; }
    public RowStatus getStatus() { return status; }
    public String getValidationError() { return validationError; }
    public Instant getCreatedAt() { return createdAt; }
}