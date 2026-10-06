package com.platform.ecom.marketplace.api;

import com.platform.ecom.marketplace.application.PublishService;
import com.platform.ecom.marketplace.domain.MarketplaceAccount;
import com.platform.ecom.marketplace.domain.PublishJob;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/marketplace")
public class MarketplaceController {

    private final PublishService publishService;

    public MarketplaceController(PublishService publishService) {
        this.publishService = publishService;
    }

    public record RegisterAccountRequest(@NotBlank String marketplaceName, @NotBlank String accountName, String credentialsJson) {}
    public record PublishRequest(
            @NotNull UUID listingId,
            @NotNull UUID listingVersionId,
            @NotNull UUID marketplaceAccountId,
            @NotBlank String title,
            String description,
            String bulletPoints,
            String keywords,
            BigDecimal priceAmount,
            String priceCurrency
    ) {}

    @PostMapping("/accounts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MarketplaceAccount> registerAccount(@Valid @RequestBody RegisterAccountRequest request) {
        MarketplaceAccount account = publishService.registerAccount(
                request.marketplaceName(), request.accountName(), request.credentialsJson()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping("/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<PublishJob> publish(@Valid @RequestBody PublishRequest request) {
        PublishJob job = publishService.executePublish(
                request.listingId(),
                request.listingVersionId(),
                request.marketplaceAccountId(),
                request.title(),
                request.description(),
                request.bulletPoints(),
                request.keywords(),
                request.priceAmount(),
                request.priceCurrency()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(job);
    }
}
