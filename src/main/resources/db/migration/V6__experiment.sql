CREATE TABLE experiments (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    listing_id UUID NOT NULL REFERENCES listings(id),
    name TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_experiments_tenant_id ON experiments (tenant_id);
CREATE INDEX idx_experiments_listing_id ON experiments (listing_id);

CREATE TABLE experiment_variants (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    experiment_id UUID NOT NULL REFERENCES experiments(id),
    listing_version_id UUID NOT NULL REFERENCES listing_versions(id),
    variant_name TEXT NOT NULL,
    traffic_weight INT NOT NULL DEFAULT 50
);

CREATE INDEX idx_exp_variants_tenant_id ON experiment_variants (tenant_id);
CREATE INDEX idx_exp_variants_exp_id ON experiment_variants (experiment_id);

CREATE TABLE metric_events (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    experiment_id UUID NOT NULL REFERENCES experiments(id),
    variant_id UUID NOT NULL REFERENCES experiment_variants(id),
    event_type VARCHAR(32) NOT NULL,
    event_value NUMERIC(12, 2) NOT NULL DEFAULT 1.0,
    recorded_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_metric_events_tenant_id ON metric_events (tenant_id);
CREATE INDEX idx_metric_events_exp_var ON metric_events (experiment_id, variant_id);
