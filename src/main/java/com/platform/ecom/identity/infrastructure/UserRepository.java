package com.platform.ecom.identity.infrastructure;

import com.platform.ecom.identity.domain.Role;
import com.platform.ecom.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByTenantIdAndEmail(UUID tenantId, String email);

    // Backs the "tenant must keep >=1 ADMIN" check in UserManagementService
    // (Step 3) — counting is cheaper and less error-prone than loading every
    // User for a tenant just to filter by role in application code.
    long countByTenantIdAndRole(UUID tenantId, Role role);
}