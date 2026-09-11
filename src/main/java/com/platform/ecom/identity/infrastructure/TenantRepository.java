package com.platform.ecom.identity.infrastructure;

import com.platform.ecom.identity.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}