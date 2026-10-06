package com.platform.ecom.listing.domain;

import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.exception.InvariantViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ListingDomainTest {

    private final UUID tenantId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final Money price = Money.of(new BigDecimal("29.99"), "USD");

    @Test
    @DisplayName("Creation produces DRAFT listing with version 1 in PENDING validation state")
    void creationInitialState() {
        Listing listing = Listing.create(
                tenantId,
                productId,
                "Valid High Converting Title",
                "Great product description that satisfies character requirements",
                "Bullet point 1",
                "keywords, search",
                price,
                "user1"
        );

        assertEquals(ListingStatus.DRAFT, listing.getStatus());
        assertEquals(1, listing.getVersions().size());
        ListingVersion v1 = listing.getVersions().getFirst();
        assertEquals(1, v1.getVersionNumber());
        assertEquals(ValidationStatus.PENDING, v1.getValidationStatus());
    }

    @Test
    @DisplayName("runValidation transitions DRAFT listing to READY_FOR_REVIEW when content is valid")
    void validationSuccessTransitionsState() {
        Listing listing = Listing.create(
                tenantId,
                productId,
                "Valid High Converting Title",
                "Great product description that satisfies character requirements",
                "Bullet point 1",
                "keywords, search",
                price,
                "user1"
        );
        UUID v1Id = listing.getVersions().getFirst().getId();

        ListingValidationEngine.ValidationResult result = listing.runValidation(v1Id, RuleSet.defaultConfig());

        assertTrue(result.isValid());
        assertEquals(ListingStatus.READY_FOR_REVIEW, listing.getStatus());
        assertEquals(ValidationStatus.VALID, listing.getVersions().getFirst().getValidationStatus());
    }

    @Test
    @DisplayName("runValidation with banned claims marks version INVALID and keeps status DRAFT")
    void validationBannedClaimFailure() {
        Listing listing = Listing.create(
                tenantId,
                productId,
                "Valid Title",
                "This comes with 100% free shipping and miracle results",
                "Bullet point 1",
                "keywords",
                price,
                "user1"
        );
        UUID v1Id = listing.getVersions().getFirst().getId();

        ListingValidationEngine.ValidationResult result = listing.runValidation(v1Id, RuleSet.defaultConfig());

        assertFalse(result.isValid());
        assertEquals(ListingStatus.DRAFT, listing.getStatus());
        assertEquals(ValidationStatus.INVALID, listing.getVersions().getFirst().getValidationStatus());
        assertTrue(result.errorsAsString().contains("banned claim"));
    }

    @Test
    @DisplayName("approve throws InvariantViolationException if status is not READY_FOR_REVIEW")
    void approveFailsWhenNotInReadyForReview() {
        Listing listing = Listing.create(
                tenantId,
                productId,
                "Valid Title",
                "Valid Description",
                "Bullet 1",
                "keywords",
                price,
                "user1"
        );
        UUID v1Id = listing.getVersions().getFirst().getId();

        assertThrows(InvariantViolationException.class, () -> listing.approve(v1Id, "reviewer1"));
    }

    @Test
    @DisplayName("approve succeeds when in READY_FOR_REVIEW and sets current version")
    void approveSucceeds() {
        Listing listing = Listing.create(
                tenantId,
                productId,
                "Valid High Converting Title",
                "Great product description that satisfies character requirements",
                "Bullet point 1",
                "keywords, search",
                price,
                "user1"
        );
        UUID v1Id = listing.getVersions().getFirst().getId();
        listing.runValidation(v1Id, RuleSet.defaultConfig());

        listing.approve(v1Id, "reviewer1");

        assertEquals(ListingStatus.APPROVED, listing.getStatus());
        assertNotNull(listing.getCurrentVersion());
        assertEquals(v1Id, listing.getCurrentVersion().getId());
    }
}
