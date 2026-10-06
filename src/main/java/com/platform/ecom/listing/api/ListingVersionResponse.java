package com.platform.ecom.listing.api;

import com.platform.ecom.listing.domain.ListingVersion;
import com.platform.ecom.listing.domain.ValidationStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListingVersionResponse(
        UUID id,
        int versionNumber,
        String title,
        String description,
        String bulletPoints,
        String keywords,
        BigDecimal priceAmount,
        String priceCurrency,
        ValidationStatus validationStatus,
        String validationErrors,
        String createdBy,
        Instant createdAt
) {
    public static ListingVersionResponse from(ListingVersion v) {
        if (v == null) return null;
        return new ListingVersionResponse(
                v.getId(),
                v.getVersionNumber(),
                v.getTitle(),
                v.getDescription(),
                v.getBulletPoints(),
                v.getKeywords(),
                v.getPrice() != null ? v.getPrice().amount() : null,
                v.getPrice() != null ? v.getPrice().currency().getCurrencyCode() : null,
                v.getValidationStatus(),
                v.getValidationErrors(),
                v.getCreatedBy(),
                v.getCreatedAt()
        );
    }
}
