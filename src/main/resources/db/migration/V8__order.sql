CREATE TABLE orders (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    external_order_id TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    total_currency TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_orders_tenant_external_id UNIQUE (tenant_id, external_order_id)
);

CREATE INDEX idx_orders_tenant_id ON orders (tenant_id);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    sku TEXT NOT NULL,
    quantity INT NOT NULL,
    unit_price_amount NUMERIC(12, 2) NOT NULL,
    unit_price_currency TEXT NOT NULL
);

CREATE INDEX idx_order_items_tenant_id ON order_items (tenant_id);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
