package com.platform.ecom.inventory.domain;

import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "stock_levels")
public class StockLevel extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String sku;

    @Column(name = "available_quantity", nullable = false)
    private int availableQuantity;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    private long version;

    protected StockLevel() {
    }

    public StockLevel(UUID tenantId, UUID productId, String sku, int initialAvailable) {
        super(tenantId);
        if (initialAvailable < 0) {
            throw new InvariantViolationException("Initial available quantity cannot be negative");
        }
        this.id = UUID.randomUUID();
        this.productId = productId;
        this.sku = sku;
        this.availableQuantity = initialAvailable;
        this.reservedQuantity = 0;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) {
            throw new InvariantViolationException("Reservation quantity must be positive");
        }
        if (availableQuantity < quantity) {
            throw new InvariantViolationException("Insufficient stock for SKU " + sku + ". Available: " + availableQuantity + ", requested: " + quantity);
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    public void release(int quantity) {
        if (quantity <= 0) {
            throw new InvariantViolationException("Release quantity must be positive");
        }
        if (reservedQuantity < quantity) {
            throw new InvariantViolationException("Cannot release more than reserved stock");
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    public void allocate(int quantity) {
        if (quantity <= 0) {
            throw new InvariantViolationException("Allocation quantity must be positive");
        }
        if (reservedQuantity < quantity) {
            throw new InvariantViolationException("Cannot allocate more than reserved stock");
        }
        this.reservedQuantity -= quantity;
    }

    public void addStock(int quantity) {
        if (quantity <= 0) {
            throw new InvariantViolationException("Stock addition quantity must be positive");
        }
        this.availableQuantity += quantity;
    }

    public UUID getId() { return id; }
    public UUID getProductId() { return productId; }
    public String getSku() { return sku; }
    public int getAvailableQuantity() { return availableQuantity; }
    public int getReservedQuantity() { return reservedQuantity; }
}
