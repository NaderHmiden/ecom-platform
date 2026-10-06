package com.platform.ecom.order.domain;

import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "external_order_id", nullable = false)
    private String externalOrderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "total_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "total_currency"))
    })
    private Money totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Order() {
    }

    public Order(UUID tenantId, String externalOrderId, String currencyCode) {
        super(tenantId);
        if (externalOrderId == null || externalOrderId.isBlank()) {
            throw new InvariantViolationException("External order ID is required");
        }
        this.id = UUID.randomUUID();
        this.externalOrderId = externalOrderId;
        this.status = OrderStatus.CREATED;
        this.totalAmount = Money.of(BigDecimal.ZERO, currencyCode != null ? currencyCode : "USD");
        this.createdAt = Instant.now();
    }

    public void addItem(UUID productId, String sku, int quantity, Money unitPrice) {
        if (status != OrderStatus.CREATED) {
            throw new InvariantViolationException("Items can only be added when order is in CREATED status");
        }
        OrderItem item = new OrderItem(getTenantId(), this, productId, sku, quantity, unitPrice);
        items.add(item);
        recalculateTotal();
    }

    private void recalculateTotal() {
        if (items.isEmpty()) {
            return;
        }
        String currency = items.getFirst().getUnitPrice().currency().getCurrencyCode();
        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().amount().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = Money.of(total, currency);
    }

    public void markProcessing() {
        if (status != OrderStatus.CREATED) {
            throw new InvariantViolationException("Cannot process order in status " + status);
        }
        this.status = OrderStatus.PROCESSING;
    }

    public void markCompleted() {
        if (status != OrderStatus.PROCESSING) {
            throw new InvariantViolationException("Cannot complete order in status " + status);
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new InvariantViolationException("Cannot cancel completed order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public UUID getId() { return id; }
    public String getExternalOrderId() { return externalOrderId; }
    public OrderStatus getStatus() { return status; }
    public Money getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
    public Instant getCreatedAt() { return createdAt; }
}
