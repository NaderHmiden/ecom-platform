package com.platform.ecom.marketplace.domain;

import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "publish_jobs")
public class PublishJob extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @Column(name = "listing_version_id", nullable = false)
    private UUID listingVersionId;

    @Column(name = "marketplace_account_id", nullable = false)
    private UUID marketplaceAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublishStatus status;

    @Column(name = "external_publish_id")
    private String externalPublishId;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PublishJob() {
    }

    public PublishJob(UUID tenantId, UUID listingId, UUID listingVersionId, UUID marketplaceAccountId) {
        super(tenantId);
        this.id = UUID.randomUUID();
        this.listingId = listingId;
        this.listingVersionId = listingVersionId;
        this.marketplaceAccountId = marketplaceAccountId;
        this.status = PublishStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markInProgress() {
        if (this.status != PublishStatus.PENDING && this.status != PublishStatus.FAILED) {
            throw new InvariantViolationException("Cannot start publish job in status " + this.status);
        }
        this.status = PublishStatus.IN_PROGRESS;
        this.updatedAt = Instant.now();
    }

    public void markSuccess(String externalPublishId) {
        this.status = PublishStatus.SUCCESS;
        this.externalPublishId = externalPublishId;
        this.errorMessage = null;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = PublishStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getListingId() { return listingId; }
    public UUID getListingVersionId() { return listingVersionId; }
    public UUID getMarketplaceAccountId() { return marketplaceAccountId; }
    public PublishStatus getStatus() { return status; }
    public String getExternalPublishId() { return externalPublishId; }
    public String getErrorMessage() { return errorMessage; }
    public int getRetryCount() { return retryCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
