package com.platform.ecom.listing.api;

import com.platform.ecom.listing.application.ListingAiGenerationService;
import com.platform.ecom.listing.application.ListingService;
import com.platform.ecom.listing.domain.Listing;
import com.platform.ecom.listing.domain.ListingValidationEngine;
import com.platform.ecom.listing.domain.ListingVersion;
import com.platform.ecom.shared.Money;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
public class ListingController {

    private final ListingService listingService;
    private final ListingAiGenerationService aiGenerationService;

    public ListingController(ListingService listingService, ListingAiGenerationService aiGenerationService) {
        this.listingService = listingService;
        this.aiGenerationService = aiGenerationService;
    }

    @PostMapping("/{listingId}/generate-ai")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ListingVersionResponse> generateAiContent(
            @PathVariable UUID listingId,
            @RequestParam UUID productId,
            @RequestParam(required = false, defaultValue = "ai-generator") String createdBy) {
        ListingVersion version = aiGenerationService.generateListingVersion(listingId, productId, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ListingVersionResponse.from(version));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ListingResponse> createListing(@Valid @RequestBody CreateListingRequest request) {
        Money price = (request.priceAmount() != null && request.priceCurrency() != null)
                ? Money.of(request.priceAmount(), request.priceCurrency())
                : null;

        Listing listing = listingService.createListing(
                request.productId(),
                request.title(),
                request.description(),
                request.bulletPoints(),
                request.keywords(),
                price,
                request.createdBy()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ListingResponse.from(listing));
    }

    @PostMapping("/{listingId}/versions")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ListingVersionResponse> addVersion(@PathVariable UUID listingId,
                                                              @Valid @RequestBody CreateListingRequest request) {
        Money price = (request.priceAmount() != null && request.priceCurrency() != null)
                ? Money.of(request.priceAmount(), request.priceCurrency())
                : null;

        ListingVersion version = listingService.addVersion(
                listingId,
                request.title(),
                request.description(),
                request.bulletPoints(),
                request.keywords(),
                price,
                request.createdBy()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ListingVersionResponse.from(version));
    }

    @PostMapping("/{listingId}/versions/{versionId}/validate")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ListingValidationEngine.ValidationResult> validateAndSubmit(
            @PathVariable UUID listingId,
            @PathVariable UUID versionId) {
        ListingValidationEngine.ValidationResult result = listingService.validateAndSubmit(listingId, versionId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{listingId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    public ResponseEntity<Void> approveListing(@PathVariable UUID listingId,
                                               @Valid @RequestBody ReviewDecisionRequest request) {
        listingService.approve(listingId, request.versionId(), request.reviewerId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{listingId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
    public ResponseEntity<Void> rejectListing(@PathVariable UUID listingId,
                                              @Valid @RequestBody ReviewDecisionRequest request) {
        listingService.reject(listingId, request.versionId(), request.reviewerId(), request.reason());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{listingId}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> publishListing(@PathVariable UUID listingId,
                                               @RequestParam(required = false, defaultValue = "operator") String publisherId) {
        listingService.publish(listingId, publisherId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{listingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'REVIEWER')")
    public ResponseEntity<ListingResponse> getListing(@PathVariable UUID listingId) {
        Listing listing = listingService.getListing(listingId);
        return ResponseEntity.ok(ListingResponse.from(listing));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'REVIEWER')")
    public ResponseEntity<List<ListingResponse>> listListings() {
        List<Listing> listings = listingService.listListings();
        return ResponseEntity.ok(listings.stream().map(ListingResponse::from).toList());
    }
}
