package com.platform.ecom.identity.domain;

import com.platform.ecom.shared.Email;
import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_users_tenant_email",
                columnNames = {"tenant_id", "email"}
        )
)
public class User extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected User() {
        // required by Hibernate — never called directly
    }

    private User(UUID id, UUID tenantId, String email, String passwordHash, Role role, Instant createdAt) {
        super(tenantId);
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public static User register(UUID tenantId, Email email, String passwordHash, Role role) {
        if (tenantId == null) {
            throw new InvariantViolationException("User must belong to a tenant");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new InvariantViolationException("User must have a password hash");
        }
        return new User(UUID.randomUUID(), tenantId, email.value(), passwordHash, role, Instant.now());
    }

    public void changeRole(Role newRole) {
        // The "tenant must keep >=1 ADMIN" check happens in
        // UserManagementService BEFORE this is called — this method trusts
        // its caller because it structurally cannot check the invariant
        // itself (see Tenant.java for the same note from the other side).
        this.role = newRole;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}