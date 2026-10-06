package com.platform.ecom.listing.application;

import com.platform.ecom.listing.domain.Listing;
import com.platform.ecom.listing.domain.ListingValidationEngine;
import com.platform.ecom.listing.domain.ListingVersion;
import com.platform.ecom.listing.domain.RuleSet;
import com.platform.ecom.listing.infrastructure.ListingRepository;
import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListingTransactionOps {

    private final ListingRepository listingRepository;

    public ListingTransactionOps(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Transactional
    public Listing createListing(UUID tenantId,
                                  UUID productId,
                                  String title,
                                  String description,
                                  String bulletPoints,
                                  String keywords,
                                  Money price,
                                  String createdBy) {
        Listing listing = Listing.create(tenantId, productId, title, description, bulletPoints, keywords, price, createdBy);
        return listingRepository.save(listing);
    }

    @Transactional
    public ListingVersion addNextVersion(UUID tenantId,
                                         UUID listingId,
                                         String title,
                                         String description,
                                         String bulletPoints,
                                         String keywords,
                                         Money price,
                                         String createdBy) {
        Listing listing = listingRepository.findByIdAndTenantId(listingId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
        ListingVersion version = listing.addVersion(title, description, bulletPoints, keywords, price, createdBy);
        listingRepository.save(listing);
        return version;
    }

    @Transactional
    public ListingValidationEngine.ValidationResult validateAndSubmit(UUID tenantId, UUID listingId, UUID versionId, RuleSet ruleSet) {
        Listing listing = listingRepository.findByIdAndTenantId(listingId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
        ListingValidationEngine.ValidationResult result = listing.runValidation(versionId, ruleSet);
        listingRepository.save(listing);
        return result;
    }

    @Transactional
    public void approveListing(UUID tenantId, UUID listingId, UUID versionId, String reviewerId) {
        Listing listing = listingRepository.findByIdAndTenantId(listingId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
        listing.approve(versionId, reviewerId);
        listingRepository.save(listing);
    }

    @Transactional
    public void rejectListing(UUID tenantId, UUID listingId, UUID versionId, String reviewerId, String reason) {
        Listing listing = listingRepository.findByIdAndTenantId(listingId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
        listing.reject(versionId, reviewerId, reason);
        listingRepository.save(listing);
    }

    @Transactional
    public void publishListing(UUID tenantId, UUID listingId, String publisherId) {
        Listing listing = listingRepository.findByIdAndTenantId(listingId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
        listing.publish(publisherId);
        listingRepository.save(listing);
    }
}
