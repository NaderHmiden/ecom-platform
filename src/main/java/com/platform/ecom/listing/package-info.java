/**
 * Module: listing
 *
 * Purpose: CORE DOMAIN. Owns the Listing/ListingVersion lifecycle state
 * machine, deterministic validation, and human review workflow. This is
 * where the project's actual value and complexity live (Phase 0 domain
 * map: core vs. supporting vs. generic domains).
 *
 * Dependency rule: depends on shared, identity, catalog (reads product
 * data), ai (via a port — see ai module doc), and marketplace (to trigger
 * publish jobs via a port — see marketplace module doc). listing's own
 * lifecycle state machine does not know marketplace-specific details;
 * it only knows "publish requested" / "publish succeeded" / "publish
 * failed" (Phase 2, ADR-003: command + async event at this boundary).
 */
package com.platform.ecom.listing;
