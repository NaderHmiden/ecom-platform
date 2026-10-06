package com.platform.ecom.marketplace.infrastructure;

import com.platform.ecom.marketplace.domain.PublishJob;
import com.platform.ecom.marketplace.domain.PublishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PublishJobRepository extends JpaRepository<PublishJob, UUID> {
    Optional<PublishJob> findByIdAndTenantId(UUID id, UUID tenantId);
    List<PublishJob> findByListingIdAndTenantId(UUID listingId, UUID tenantId);
    boolean existsByListingIdAndMarketplaceAccountIdAndStatusAndTenantId(
            UUID listingId, UUID marketplaceAccountId, PublishStatus status, UUID tenantId);
}
