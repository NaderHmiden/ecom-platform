CREATE TABLE marketplace_accounts (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    marketplace_name TEXT NOT NULL,
    account_name TEXT NOT NULL,
    credentials_json TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_mp_accounts_tenant_id ON marketplace_accounts (tenant_id);

CREATE TABLE publish_jobs (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    listing_id UUID NOT NULL REFERENCES listings(id),
    listing_version_id UUID NOT NULL REFERENCES listing_versions(id),
    marketplace_account_id UUID NOT NULL REFERENCES marketplace_accounts(id),
    status VARCHAR(32) NOT NULL,
    external_publish_id TEXT,
    error_message TEXT,
    retry_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_publish_jobs_tenant_id ON publish_jobs (tenant_id);
CREATE INDEX idx_publish_jobs_listing_id ON publish_jobs (listing_id);

-- Concurrency Invariant Index: Only ONE job per (listing, account) in IN_PROGRESS status
CREATE UNIQUE INDEX uk_publish_jobs_in_progress
    ON publish_jobs (listing_id, marketplace_account_id)
    WHERE status = 'IN_PROGRESS';
