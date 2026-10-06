package com.platform.ecom.experiment.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddVariantRequest(
        @NotNull UUID listingVersionId,
        @NotBlank String variantName,
        int trafficWeight
) {
}
