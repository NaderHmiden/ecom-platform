/**
 * Module: catalog
 *
 * Purpose: the canonical, trustworthy product truth for a tenant
 * (Product, ProductVariant, Category, Attribute). This is what "listing"
 * generates content from.
 *
 * Dependency rule: depends on shared, identity, and supplier's published
 * output. listing depends on catalog; catalog must never depend on
 * listing (Phase 2 context map).
 *
 * Note (Phase 2 decision): catalog does NOT publish a "ProductNormalized"
 * integration event yet. Nothing currently needs to react automatically
 * to normalization — listing generation is operator-initiated. Add that
 * event only when a real consumer needs it.
 */
package com.platform.ecom.catalog;
