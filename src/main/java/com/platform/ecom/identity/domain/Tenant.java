package com.platform.ecom.identity.domain;

import com.platform.ecom.shared.exception.InvariantViolationException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Tenant() {
        // required by Hibernate — never called directly
    }

    private Tenant(UUID id, String name, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public static Tenant create(String name) {
        if (name == null || name.isBlank()) {
            throw new InvariantViolationException("Tenant name must not be blank");
        }
        return new Tenant(UUID.randomUUID(), name.trim(), Instant.now());
    }

    // Deliberately NOT enforced here: "a tenant must always have >=1 ADMIN
    // user." Tenant, as an aggregate, has no visibility into User records —
    // that's a cross-aggregate invariant, enforced in UserManagementService
    // (Phase 4, Step 3), which can query across both.

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}