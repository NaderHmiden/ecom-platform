CREATE TABLE tenants (
                         id UUID PRIMARY KEY,
                         name TEXT NOT NULL,
                         created_at TIMESTAMPTZ NOT NULL,
                         version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       tenant_id UUID NOT NULL REFERENCES tenants(id),
                       email TEXT NOT NULL,
                       password_hash TEXT NOT NULL,
                       role TEXT NOT NULL,
                       created_at TIMESTAMPTZ NOT NULL,
                       version BIGINT NOT NULL DEFAULT 0,
                       CONSTRAINT uk_users_tenant_email UNIQUE (tenant_id, email)
);

-- Every tenant-scoped query filters by tenant_id (Phase 2, ADR-002) —
-- this index is what keeps that filter cheap as the users table grows.
CREATE INDEX idx_users_tenant_id ON users (tenant_id);