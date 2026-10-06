package com.platform.ecom.listing.infrastructure;

import com.platform.ecom.listing.domain.ListingVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ListingVersionRepository extends JpaRepository<ListingVersion, UUID> {
    Optional<ListingVersion> findByIdAndTenantId(UUID id, UUID tenantId);
    List<ListingVersion> findByListingIdAndTenantId(UUID listingId, UUID tenantId);
}
