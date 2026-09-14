package com.platform.ecom.shared;

import java.util.Optional;

/**
 * Holds "which tenant is this request for" during processing.
 *
 * Deliberately Optional-returning, not throwing-if-absent: unauthenticated
 * flows (login lookup, tenant provisioning) have no tenant context yet and
 * must keep working — they rely on explicit tenantId parameters instead
 * (see UserRepository, Step 2). This class is the automatic layer on top
 * of that, not a replacement for it.
 */
public final class TenantContext {

    private static final ThreadLocal<TenantId> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(TenantId tenantId) {
        CURRENT.set(tenantId);
    }

    public static Optional<TenantId> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * For call sites that only make sense inside an authenticated request
     * (most controllers) — fails loudly instead of silently proceeding
     * unscoped if it's ever called somewhere TenantContext was never set.
     */
    public static TenantId currentOrThrow() {
        return current().orElseThrow(() ->
                new IllegalStateException("No tenant context set — this code path requires an authenticated request"));
    }

    /**
     * MUST be called at the end of every request (see JwtAuthenticationFilter's
     * finally block). Threads are reused from a pool — without this, the
     * next unrelated request handled by the same thread would silently
     * inherit the previous request's tenant.
     */
    public static void clear() {
        CURRENT.remove();
    }
}