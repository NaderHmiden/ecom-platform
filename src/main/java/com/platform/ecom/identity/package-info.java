/**
 * Module: identity
 *
 * Purpose: authentication, users, roles, and tenants (Phase 1 domain
 * discovery folded "tenant" into this module — User and Tenant are too
 * tightly coupled to justify separate modules yet).
 *
 * Dependency rule: identity depends only on shared. Every other business
 * module depends on identity for tenant scoping and authorization checks,
 * but identity depends on none of them — it is the foundation layer
 * (Phase 2, module dependency graph).
 */
package com.platform.ecom.identity;
