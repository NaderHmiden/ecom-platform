package com.platform.ecom.listing.domain;

import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "listings")
public class Listing extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private ListingVersion currentVersion;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ListingVersion> versions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Listing() {
        // required by Hibernate
    }

    private Listing(UUID tenantId, UUID productId) {
        super(tenantId);
        if (productId == null) {
            throw new InvariantViolationException("Listing must be linked to a product ID");
        }
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.status = ListingStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public static Listing create(UUID tenantId,
                                 UUID productId,
                                 String title,
                                 String description,
                                 String bulletPoints,
                                 String keywords,
                                 Money price,
                                 String createdBy) {
        Listing listing = new Listing(tenantId, productId);
        listing.addVersion(title, description, bulletPoints, keywords, price, createdBy);
        return listing;
    }

    public ListingVersion addVersion(String title,
                                     String description,
                                     String bulletPoints,
                                     String keywords,
                                     Money price,
                                     String createdBy) {
        int nextVersionNum = versions.size() + 1;
        ListingVersion listingVersion = new ListingVersion(
                getTenantId(),
                this,
                nextVersionNum,
                title,
                description,
                bulletPoints,
                keywords,
                price,
                createdBy
        );
        versions.add(listingVersion);
        if (this.status == ListingStatus.REJECTED) {
            this.status = ListingStatus.DRAFT;
        }
        this.updatedAt = Instant.now();
        return listingVersion;
    }

    public ListingValidationEngine.ValidationResult runValidation(UUID versionId, RuleSet ruleSet) {
        ListingVersion target = findVersion(versionId);
        ListingValidationEngine.ValidationResult result = ListingValidationEngine.validate(target, ruleSet);
        if (result.isValid()) {
            target.markValid();
            if (this.status == ListingStatus.DRAFT || this.status == ListingStatus.REJECTED) {
                this.status = ListingStatus.READY_FOR_REVIEW;
            }
        } else {
            target.markInvalid(result.errorsAsString());
        }
        this.updatedAt = Instant.now();
        return result;
    }

    public void approve(UUID versionId, String reviewerId) {
        if (this.status != ListingStatus.READY_FOR_REVIEW) {
            throw new InvariantViolationException(
                    "Cannot approve listing in status " + this.status + ". Must be READY_FOR_REVIEW."
            );
        }
        ListingVersion target = findVersion(versionId);
        if (target.getValidationStatus() != ValidationStatus.VALID) {
            throw new InvariantViolationException(
                    "Cannot approve listing version " + versionId + " because its validation status is " + target.getValidationStatus()
            );
        }
        this.currentVersion = target;
        this.status = ListingStatus.APPROVED;
        this.updatedAt = Instant.now();
    }

    public void reject(UUID versionId, String reviewerId, String reason) {
        if (this.status != ListingStatus.READY_FOR_REVIEW) {
            throw new InvariantViolationException(
                    "Cannot reject listing in status " + this.status + ". Must be READY_FOR_REVIEW."
            );
        }
        this.status = ListingStatus.REJECTED;
        this.updatedAt = Instant.now();
    }

    public void publish(String publisherId) {
        if (this.status != ListingStatus.APPROVED) {
            throw new InvariantViolationException(
                    "Cannot publish listing in status " + this.status + ". Must be APPROVED first."
            );
        }
        this.status = ListingStatus.PUBLISHED;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        this.status = ListingStatus.ARCHIVED;
        this.updatedAt = Instant.now();
    }

    public ListingVersion findVersion(UUID versionId) {
        return versions.stream()
                .filter(v -> v.getId().equals(versionId))
                .findFirst()
                .orElseThrow(() -> new InvariantViolationException("Listing version not found: " + versionId));
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public ListingStatus getStatus() { return status; }
    public ListingVersion getCurrentVersion() { return currentVersion; }
    public List<ListingVersion> getVersions() { return List.copyOf(versions); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
