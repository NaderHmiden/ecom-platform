package com.platform.ecom.listing.application;

import com.platform.ecom.ai.infrastructure.MockGenerationAdapter;
import com.platform.ecom.catalog.domain.Product;
import com.platform.ecom.catalog.infrastructure.ProductRepository;
import com.platform.ecom.listing.domain.Listing;
import com.platform.ecom.listing.domain.ListingStatus;
import com.platform.ecom.listing.domain.ListingVersion;
import com.platform.ecom.listing.domain.ValidationStatus;
import com.platform.ecom.listing.infrastructure.ListingRepository;
import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ListingAiGenerationServiceTest {

    private final UUID tenantId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Money price = Money.of(new BigDecimal("19.99"), "USD");

    private ListingRepository listingRepository;
    private ProductRepository productRepository;
    private ListingAiGenerationService aiService;

    @BeforeEach
    void setUp() {
        TenantContext.set(new TenantId(tenantId));
        listingRepository = Mockito.mock(ListingRepository.class);
        productRepository = Mockito.mock(ProductRepository.class);

        ListingTransactionOps transactionOps = new ListingTransactionOps(listingRepository);
        aiService = new ListingAiGenerationService(new MockGenerationAdapter(), transactionOps, productRepository);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("generateListingVersion produces AI copy, validates, and sets status to READY_FOR_REVIEW")
    void generateAiListingVersionPipeline() {
        Product mockProduct = Product.createWithInitialVariant(
                tenantId,
                "Wireless Headphones",
                "Electronics",
                null,
                "SKU-123",
                price,
                "{}"
        );
        when(productRepository.findByIdAndTenantId(productId, tenantId)).thenReturn(Optional.of(mockProduct));

        Listing listing = Listing.create(
                tenantId,
                productId,
                "Initial Title",
                "Initial Description",
                "Bullet",
                "key",
                price,
                "user"
        );
        when(listingRepository.findByIdAndTenantId(listing.getId(), tenantId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ListingVersion generatedVersion = aiService.generateListingVersion(listing.getId(), productId, "ai-bot");

        assertNotNull(generatedVersion);
        assertEquals(2, generatedVersion.getVersionNumber());
        assertTrue(generatedVersion.getTitle().contains("Wireless Headphones"));
        assertEquals(ValidationStatus.VALID, generatedVersion.getValidationStatus());
        assertEquals(ListingStatus.READY_FOR_REVIEW, listing.getStatus());
    }
}
