-- V1: baseline. No domain tables yet (Phase 4 starts those with identity/tenant).
-- pgcrypto gives us gen_random_uuid() for UUID primary keys across all modules.
CREATE EXTENSION IF NOT EXISTS pgcrypto;