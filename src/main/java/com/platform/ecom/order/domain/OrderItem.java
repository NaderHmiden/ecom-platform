package com.platform.ecom.order.domain;

import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem extends TenantScopedEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private int quantity;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;

    protected OrderItem() {
    }

    public OrderItem(UUID tenantId, Order order, UUID productId, String sku, int quantity, Money unitPrice) {
        super(tenantId);
        if (quantity <= 0) {
            throw new InvariantViolationException("OrderItem quantity must be greater than zero");
        }
        if (unitPrice == null) {
            throw new InvariantViolationException("OrderItem unitPrice is required");
        }
        this.id = UUID.randomUUID();
        this.order = order;
        this.productId = productId;
        this.sku = sku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public UUID getId() { return id; }
    public Order getOrder() { return order; }
    public UUID getProductId() { return productId; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
    public Money getUnitPrice() { return unitPrice; }
}
