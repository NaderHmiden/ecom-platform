/**
 * Module: shared
 *
 * Purpose: cross-cutting value objects and base types with no business
 * meaning of their own (e.g. Money, TenantId, domain exception base classes,
 * tenant-context plumbing).
 *
 * Dependency rule: shared depends on NOTHING else in this codebase.
 * Every other module may depend on shared. If a "shared" concept starts
 * requiring knowledge of a specific business domain, it does not belong
 * here — move it to the module that owns that knowledge (Phase 2, ADR-004
 * discussion).
 */
package com.platform.ecom.shared;
