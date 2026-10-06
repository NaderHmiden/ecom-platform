package com.platform.ecom.catalog.domain;

import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

@Entity
@Table(
        name = "product_variants",
        uniqueConstraints = @UniqueConstraint(name = "uk_variants_tenant_sku", columnNames = {"tenant_id", "sku"})
)
public class ProductVariant extends TenantScopedEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String sku;

    @Column(name = "price_amount", nullable = false)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", nullable = false, length = 3)
    private String priceCurrency;

    // Attributes as opaque JSON — deliberate simplification, same
    // reasoning as RawSupplierRow.rawDataJson: a fully typed Attribute
    // model is real complexity with no demonstrated need yet.
    @Column(name = "attributes_json", nullable = false, columnDefinition = "text")
    private String attributesJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProductVariant() {
        // required by Hibernate
    }

    // Package-private, not public: the ONLY way to create a ProductVariant
    // is through Product.addVariant()/createWithInitialVariant(). This is
    // the aggregate boundary from Phase 1 enforced with nothing more
    // exotic than Java's own access modifiers.
    ProductVariant(UUID tenantId, String sku, Money price, String attributesJson) {
        super(tenantId);
        if (sku == null || sku.isBlank()) {
            throw new InvariantViolationException("Variant SKU must not be blank");
        }
        this.id = UUID.randomUUID();
        this.sku = sku;
        this.priceAmount = price.amount();
        this.priceCurrency = price.currency().getCurrencyCode();
        this.attributesJson = attributesJson;
        this.createdAt = Instant.now();
    }

    void assignToProduct(Product product) {
        this.product = product;
    }

    public UUID getId() { return id; }
    public String getSku() { return sku; }
    public Money getPrice() { return new Money(priceAmount, Currency.getInstance(priceCurrency)); }
    public String getAttributesJson() { return attributesJson; }
    public Instant getCreatedAt() { return createdAt; }
}