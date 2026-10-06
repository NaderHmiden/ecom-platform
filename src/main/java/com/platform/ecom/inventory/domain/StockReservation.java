package com.platform.ecom.inventory.domain;

import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_reservations")
public class StockReservation extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected StockReservation() {
    }

    public StockReservation(UUID tenantId, UUID orderId, String sku, int quantity) {
        super(tenantId);
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.sku = sku;
        this.quantity = quantity;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = Instant.now();
    }

    public void markAllocated() {
        this.status = ReservationStatus.ALLOCATED;
    }

    public void markReleased() {
        this.status = ReservationStatus.RELEASED;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public ReservationStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
