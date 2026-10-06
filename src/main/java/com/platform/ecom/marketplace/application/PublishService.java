package com.platform.ecom.marketplace.application;

import com.platform.ecom.marketplace.domain.*;
import com.platform.ecom.marketplace.infrastructure.MarketplaceAccountRepository;
import com.platform.ecom.marketplace.infrastructure.PublishJobRepository;
import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PublishService {

    private final MarketplacePort marketplacePort;
    private final MarketplaceAccountRepository accountRepository;
    private final PublishJobRepository jobRepository;

    public PublishService(MarketplacePort marketplacePort,
                          MarketplaceAccountRepository accountRepository,
                          PublishJobRepository jobRepository) {
        this.marketplacePort = marketplacePort;
        this.accountRepository = accountRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional
    public MarketplaceAccount registerAccount(String marketplaceName, String accountName, String credentialsJson) {
        UUID tenantId = currentTenantId();
        MarketplaceAccount account = new MarketplaceAccount(tenantId, marketplaceName, accountName, credentialsJson);
        return accountRepository.save(account);
    }

    @Transactional
    public PublishJob executePublish(UUID listingId,
                                     UUID listingVersionId,
                                     UUID marketplaceAccountId,
                                     String title,
                                     String description,
                                     String bulletPoints,
                                     String keywords,
                                     BigDecimal priceAmount,
                                     String priceCurrency) {
        UUID tenantId = currentTenantId();

        MarketplaceAccount account = accountRepository.findByIdAndTenantId(marketplaceAccountId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Marketplace account not found: " + marketplaceAccountId));

        // Concurrency Invariant: Ensure no job is currently IN_PROGRESS for this (listing, account)
        boolean inProgressExists = jobRepository.existsByListingIdAndMarketplaceAccountIdAndStatusAndTenantId(
                listingId, marketplaceAccountId, PublishStatus.IN_PROGRESS, tenantId);
        if (inProgressExists) {
            throw new InvariantViolationException(
                    "A publish job is already IN_PROGRESS for listing " + listingId + " on account " + marketplaceAccountId
            );
        }

        PublishJob job = new PublishJob(tenantId, listingId, listingVersionId, marketplaceAccountId);
        job.markInProgress();
        jobRepository.save(job);

        PublishResult result = marketplacePort.publishListing(
                title,
                description,
                bulletPoints,
                keywords,
                priceAmount,
                priceCurrency,
                account.getCredentialsJson()
        );

        if (result.success()) {
            job.markSuccess(result.externalPublishId());
        } else {
            job.markFailed(result.errorMessage());
        }

        return jobRepository.save(job);
    }

    private UUID currentTenantId() {
        return TenantContext.current()
                .map(TenantId::value)
                .orElseThrow(() -> new IllegalStateException("Tenant context required for publish operations"));
    }
}
