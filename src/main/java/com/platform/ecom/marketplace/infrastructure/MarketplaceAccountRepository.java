package com.platform.ecom.marketplace.infrastructure;

import com.platform.ecom.marketplace.domain.MarketplaceAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketplaceAccountRepository extends JpaRepository<MarketplaceAccount, UUID> {
    Optional<MarketplaceAccount> findByIdAndTenantId(UUID id, UUID tenantId);
    List<MarketplaceAccount> findByTenantId(UUID tenantId);
}
