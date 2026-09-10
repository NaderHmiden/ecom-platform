/**
 * Module: supplier
 *
 * Purpose: ingesting raw, untrusted external supplier data (SupplierImportJob,
 * RawSupplierRow) and normalizing it into the shape catalog can trust.
 * This module owns the mess so no other module has to know supplier CSVs
 * are inconsistent.
 *
 * Dependency rule: depends on shared and identity (tenant scoping) only.
 * catalog may depend on supplier's published output; supplier must never
 * depend on catalog (Phase 2 context map: supplier --feeds--> catalog).
 */
package com.platform.ecom.supplier;
