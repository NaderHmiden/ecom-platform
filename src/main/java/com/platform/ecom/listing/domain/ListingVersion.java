package com.platform.ecom.listing.domain;

import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "listing_versions")
public class ListingVersion extends TenantScopedEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column(name = "bullet_points")
    private String bulletPoints;

    @Column
    private String keywords;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency"))
    })
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false)
    private ValidationStatus validationStatus;

    @Column(name = "validation_errors")
    private String validationErrors;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ListingVersion() {
        // required by Hibernate
    }

    public ListingVersion(UUID tenantId,
                          Listing listing,
                          int versionNumber,
                          String title,
                          String description,
                          String bulletPoints,
                          String keywords,
                          Money price,
                          String createdBy) {
        super(tenantId);
        this.id = UUID.randomUUID();
        this.listing = listing;
        this.versionNumber = versionNumber;
        this.title = title;
        this.description = description;
        this.bulletPoints = bulletPoints;
        this.keywords = keywords;
        this.price = price;
        this.validationStatus = ValidationStatus.PENDING;
        this.createdBy = createdBy != null ? createdBy : "system";
        this.createdAt = Instant.now();
    }

    public void markValid() {
        this.validationStatus = ValidationStatus.VALID;
        this.validationErrors = null;
    }

    public void markInvalid(String errors) {
        this.validationStatus = ValidationStatus.INVALID;
        this.validationErrors = errors;
    }

    public UUID getId() { return id; }
    public Listing getListing() { return listing; }
    public int getVersionNumber() { return versionNumber; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getBulletPoints() { return bulletPoints; }
    public String getKeywords() { return keywords; }
    public Money getPrice() { return price; }
    public ValidationStatus getValidationStatus() { return validationStatus; }
    public String getValidationErrors() { return validationErrors; }
    public String getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
