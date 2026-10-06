CREATE TABLE products (
                          id UUID PRIMARY KEY,
                          tenant_id UUID NOT NULL REFERENCES tenants(id),
                          name TEXT NOT NULL,
                          category TEXT,
                          source_row_id UUID,
                          created_at TIMESTAMPTZ NOT NULL,
                          version BIGINT NOT NULL DEFAULT 0,
    -- Postgres does not treat NULLs as equal for uniqueness, so this only
    -- guards "one supplier row -> at most one Product," not manually
    -- created products (which leave source_row_id null).
                          CONSTRAINT uk_products_tenant_source_row UNIQUE (tenant_id, source_row_id)
);

CREATE INDEX idx_products_tenant_id ON products (tenant_id);

CREATE TABLE product_variants (
                                  id UUID PRIMARY KEY,
                                  tenant_id UUID NOT NULL REFERENCES tenants(id),
                                  product_id UUID NOT NULL REFERENCES products(id),
                                  sku TEXT NOT NULL,
                                  price_amount NUMERIC(12, 2) NOT NULL,
                                  price_currency TEXT NOT NULL,
                                  attributes_json TEXT NOT NULL,
                                  created_at TIMESTAMPTZ NOT NULL,
    -- SKU unique across the tenant's WHOLE catalog, not just within one
    -- product (Phase 1: the stronger of the two invariants discussed
    -- subsumes the weaker "unique within a product" one automatically).
                                  CONSTRAINT uk_variants_tenant_sku UNIQUE (tenant_id, sku)
);

CREATE INDEX idx_variants_tenant_id ON product_variants (tenant_id);
CREATE INDEX idx_variants_product_id ON product_variants (product_id);