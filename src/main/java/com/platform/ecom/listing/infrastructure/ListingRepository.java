package com.platform.ecom.listing.infrastructure;

import com.platform.ecom.listing.domain.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    Optional<Listing> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Listing> findByTenantId(UUID tenantId);
    Optional<Listing> findByProductIdAndTenantId(UUID productId, UUID tenantId);
}
