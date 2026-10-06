package com.platform.ecom.listing.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateListingRequest(
        @NotNull UUID productId,
        @NotBlank String title,
        String description,
        String bulletPoints,
        String keywords,
        BigDecimal priceAmount,
        String priceCurrency,
        String createdBy
) {
}
