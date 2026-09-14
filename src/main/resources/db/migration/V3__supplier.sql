CREATE TABLE supplier_import_jobs (
                                      id UUID PRIMARY KEY,
                                      tenant_id UUID NOT NULL REFERENCES tenants(id),
                                      source_file_name TEXT NOT NULL,
                                      content_hash TEXT NOT NULL,
                                      status TEXT NOT NULL,
                                      total_rows INTEGER NOT NULL DEFAULT 0,
                                      succeeded_rows INTEGER NOT NULL DEFAULT 0,
                                      failed_rows INTEGER NOT NULL DEFAULT 0,
                                      created_at TIMESTAMPTZ NOT NULL,
                                      completed_at TIMESTAMPTZ,
                                      version BIGINT NOT NULL DEFAULT 0,
                                      CONSTRAINT uk_import_jobs_tenant_content_hash UNIQUE (tenant_id, content_hash)
);

CREATE INDEX idx_import_jobs_tenant_id ON supplier_import_jobs (tenant_id);

CREATE TABLE raw_supplier_rows (
                                   id UUID PRIMARY KEY,
                                   tenant_id UUID NOT NULL REFERENCES tenants(id),
                                   import_job_id UUID NOT NULL REFERENCES supplier_import_jobs(id),
                                   row_number INTEGER NOT NULL,
                                   raw_data_json TEXT NOT NULL,
                                   status TEXT NOT NULL,
                                   validation_error TEXT,
                                   created_at TIMESTAMPTZ NOT NULL
);

-- Every tenant-scoped query filters by tenant_id (ADR-002).
CREATE INDEX idx_raw_rows_tenant_id ON raw_supplier_rows (tenant_id);

-- The processing worker's primary access pattern: "give me the next rows
-- of a given status for this job."
CREATE INDEX idx_raw_rows_job_status ON raw_supplier_rows (import_job_id, status);