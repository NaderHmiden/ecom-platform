package com.platform.ecom.shared.persistence;

import com.platform.ecom.shared.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Enables the Hibernate "tenantFilter" (declared on TenantScopedEntity) on
 * the current persistence context before any Spring Data repository method
 * runs, using whatever tenant TenantContext currently holds.
 *
 * If TenantContext has nothing set — unauthenticated flows: login lookup,
 * tenant provisioning — the filter is deliberately left disabled. Those
 * code paths already pass tenantId explicitly to every query (Phase 4,
 * Step 2) as their own, independent safety net; this aspect is the
 * automatic layer for authenticated, tenant-bound requests, not a
 * replacement for that explicit parameterization (ADR-002: defense in depth).
 */
@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.platform.ecom..infrastructure..*Repository+.*(..))")
    public void enableTenantFilter() {
        TenantContext.current().ifPresent(tenantId -> {
            Session session = entityManager.unwrap(Session.class);
            if (!session.getEnabledFilterNames().contains("tenantFilter")) {
                session.enableFilter("tenantFilter").setParameter("tenantId", tenantId.value());
            }
        });
    }
}