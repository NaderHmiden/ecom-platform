package com.platform.ecom.fulfillment.domain;

import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fulfillment_requests")
public class FulfillmentRequest extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FulfillmentStatus status;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FulfillmentRequest() {
    }

    public FulfillmentRequest(UUID tenantId, UUID orderId) {
        super(tenantId);
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.status = FulfillmentStatus.PENDING;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void markAllocated() {
        if (status != FulfillmentStatus.PENDING) {
            throw new InvariantViolationException("Cannot allocate fulfillment request in status " + status);
        }
        this.status = FulfillmentStatus.ALLOCATED;
        this.updatedAt = Instant.now();
    }

    public void markShipped(String trackingNumber) {
        if (status != FulfillmentStatus.ALLOCATED) {
            throw new InvariantViolationException("Cannot ship fulfillment request unless allocated");
        }
        this.status = FulfillmentStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == FulfillmentStatus.SHIPPED) {
            throw new InvariantViolationException("Cannot cancel shipped fulfillment request");
        }
        this.status = FulfillmentStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public FulfillmentStatus getStatus() { return status; }
    public String getTrackingNumber() { return trackingNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
