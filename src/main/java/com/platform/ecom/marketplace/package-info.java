/**
 * Module: marketplace
 *
 * Purpose: publish-adapter infrastructure (MarketplacePort + per-marketplace
 * adapters), MarketplaceAccount, PublishJob. A real Adapter-pattern module —
 * full design deferred to Phase 11, target marketplace deferred by request
 * (Phase 0 decision).
 *
 * Dependency rule: depends on shared and identity only. Like ai, this
 * module must not depend on listing — listing depends on marketplace's
 * port, not vice versa, so we can add/remove marketplace adapters without
 * touching listing's domain logic.
 */
package com.platform.ecom.marketplace;
