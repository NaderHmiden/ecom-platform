package com.platform.ecom.listing.api;

import com.platform.ecom.listing.domain.Listing;
import com.platform.ecom.listing.domain.ListingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ListingResponse(
        UUID id,
        UUID productId,
        ListingStatus status,
        ListingVersionResponse currentVersion,
        List<ListingVersionResponse> versions,
        Instant createdAt,
        Instant updatedAt
) {
    public static ListingResponse from(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getProductId(),
                listing.getStatus(),
                ListingVersionResponse.from(listing.getCurrentVersion()),
                listing.getVersions().stream().map(ListingVersionResponse::from).toList(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}
