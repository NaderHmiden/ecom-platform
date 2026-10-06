CREATE TABLE stock_levels (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    product_id UUID NOT NULL REFERENCES products(id),
    sku TEXT NOT NULL,
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_stock_levels_tenant_sku UNIQUE (tenant_id, sku)
);

CREATE INDEX idx_stock_levels_tenant_id ON stock_levels (tenant_id);

CREATE TABLE stock_reservations (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    order_id UUID NOT NULL REFERENCES orders(id),
    sku TEXT NOT NULL,
    quantity INT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_stock_res_tenant_id ON stock_reservations (tenant_id);
CREATE INDEX idx_stock_res_order_id ON stock_reservations (order_id);

CREATE TABLE fulfillment_requests (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    order_id UUID NOT NULL REFERENCES orders(id),
    status VARCHAR(32) NOT NULL,
    tracking_number TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_fulfillment_tenant_id ON fulfillment_requests (tenant_id);
CREATE INDEX idx_fulfillment_order_id ON fulfillment_requests (order_id);
