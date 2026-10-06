package com.platform.ecom.listing.application;

import com.platform.ecom.listing.domain.Listing;
import com.platform.ecom.listing.domain.ListingValidationEngine;
import com.platform.ecom.listing.domain.ListingVersion;
import com.platform.ecom.listing.domain.RuleSet;
import com.platform.ecom.listing.infrastructure.ListingRepository;
import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.shared.exception.AuthenticationFailedException;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListingService {

    private final ListingTransactionOps transactionOps;
    private final ListingRepository listingRepository;

    public ListingService(ListingTransactionOps transactionOps, ListingRepository listingRepository) {
        this.transactionOps = transactionOps;
        this.listingRepository = listingRepository;
    }

    public Listing createListing(UUID productId,
                                 String title,
                                 String description,
                                 String bulletPoints,
                                 String keywords,
                                 Money price,
                                 String createdBy) {
        UUID tenantId = currentTenantId();
        return transactionOps.createListing(tenantId, productId, title, description, bulletPoints, keywords, price, createdBy);
    }

    public ListingVersion addVersion(UUID listingId,
                                     String title,
                                     String description,
                                     String bulletPoints,
                                     String keywords,
                                     Money price,
                                     String createdBy) {
        UUID tenantId = currentTenantId();
        return transactionOps.addNextVersion(tenantId, listingId, title, description, bulletPoints, keywords, price, createdBy);
    }

    public ListingValidationEngine.ValidationResult validateAndSubmit(UUID listingId, UUID versionId) {
        UUID tenantId = currentTenantId();
        return transactionOps.validateAndSubmit(tenantId, listingId, versionId, RuleSet.defaultConfig());
    }

    public void approve(UUID listingId, UUID versionId, String reviewerId) {
        UUID tenantId = currentTenantId();
        transactionOps.approveListing(tenantId, listingId, versionId, reviewerId);
    }

    public void reject(UUID listingId, UUID versionId, String reviewerId, String reason) {
        UUID tenantId = currentTenantId();
        transactionOps.rejectListing(tenantId, listingId, versionId, reviewerId, reason);
    }

    public void publish(UUID listingId, String publisherId) {
        UUID tenantId = currentTenantId();
        transactionOps.publishListing(tenantId, listingId, publisherId);
    }

    @Transactional(readOnly = true)
    public Listing getListing(UUID listingId) {
        UUID tenantId = currentTenantId();
        return listingRepository.findByIdAndTenantId(listingId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));
    }

    @Transactional(readOnly = true)
    public List<Listing> listListings() {
        UUID tenantId = currentTenantId();
        return listingRepository.findByTenantId(tenantId);
    }

    private UUID currentTenantId() {
        return TenantContext.current()
                .map(TenantId::value)
                .orElseThrow(() -> new IllegalStateException("Tenant context required for listing operations"));
    }
}
