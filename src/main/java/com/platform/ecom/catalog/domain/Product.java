package com.platform.ecom.catalog.domain;

import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    // Plain string, not a Category aggregate — deliberate scope decision,
    // see Phase 6 design notes. Revisit when a real category-hierarchy use
    // case exists (marketplace taxonomies, Phase 11).
    @Column
    private String category;

    // The RawSupplierRow this Product was created from, if any (manual
    // product creation later would leave this null). Unique per tenant
    // WHEN PRESENT — Postgres doesn't enforce uniqueness across NULLs, so
    // this only guards against one row producing two Products; it doesn't
    // restrict how many manually-created products a tenant can have.
    @Column(name = "source_row_id")
    private UUID sourceRowId;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductVariant> variants = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Product() {
        // required by Hibernate
    }

    private Product(UUID tenantId, String name, String category, UUID sourceRowId) {
        super(tenantId);
        if (name == null || name.isBlank()) {
            throw new InvariantViolationException("Product name must not be blank");
        }
        this.id = UUID.randomUUID();
        this.name = name;
        this.category = category;
        this.sourceRowId = sourceRowId;
        this.createdAt = Instant.now();
    }

    /**
     * The only way to create a Product. It deliberately REQUIRES the first
     * variant's data as parameters rather than allowing construction with
     * zero variants, then a separate addVariant() call. "A product must
     * always have at least one variant" (Phase 1) is enforced by
     * construction — there is no code path where a Product object exists,
     * even transiently, with an empty variant list.
     */
    public static Product createWithInitialVariant(UUID tenantId,
                                                   String name,
                                                   String category,
                                                   UUID sourceRowId,
                                                   String initialSku,
                                                   Money initialPrice,
                                                   String initialAttributesJson) {
        Product product = new Product(tenantId, name, category, sourceRowId);
        product.addVariant(initialSku, initialPrice, initialAttributesJson);
        return product;
    }

    public void addVariant(String sku, Money price, String attributesJson) {
        boolean duplicateWithinProduct = variants.stream().anyMatch(v -> v.getSku().equals(sku));
        if (duplicateWithinProduct) {
            throw new InvariantViolationException("Variant SKU already exists on this product: " + sku);
        }
        // getTenantId() (inherited from TenantScopedEntity) is used here
        // rather than a separate parameter — a variant's tenant can never
        // diverge from its parent product's tenant by construction.
        ProductVariant variant = new ProductVariant(getTenantId(), sku, price, attributesJson);
        variant.assignToProduct(this);
        variants.add(variant);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public UUID getSourceRowId() { return sourceRowId; }
    public Instant getCreatedAt() { return createdAt; }

    // Immutable copy — callers can read variants but cannot add/remove
    // from the list directly, bypassing addVariant()'s invariant checks.
    public List<ProductVariant> getVariants() {
        return List.copyOf(variants);
    }
}