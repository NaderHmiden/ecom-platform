CREATE TABLE listings (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    product_id UUID NOT NULL REFERENCES products(id),
    status VARCHAR(32) NOT NULL,
    current_version_id UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_listings_tenant_id ON listings (tenant_id);
CREATE INDEX idx_listings_product_id ON listings (product_id);

CREATE TABLE listing_versions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    listing_id UUID NOT NULL REFERENCES listings(id),
    version_number INT NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    bullet_points TEXT,
    keywords TEXT,
    price_amount NUMERIC(12, 2),
    price_currency TEXT,
    validation_status VARCHAR(32) NOT NULL,
    validation_errors TEXT,
    created_by TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_listing_versions_number UNIQUE (listing_id, version_number)
);

CREATE INDEX idx_listing_versions_tenant_id ON listing_versions (tenant_id);
CREATE INDEX idx_listing_versions_listing_id ON listing_versions (listing_id);

ALTER TABLE listings ADD CONSTRAINT fk_listings_current_version FOREIGN KEY (current_version_id) REFERENCES listing_versions(id);
