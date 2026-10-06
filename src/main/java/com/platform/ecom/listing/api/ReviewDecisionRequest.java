package com.platform.ecom.listing.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReviewDecisionRequest(
        @NotNull UUID versionId,
        String reviewerId,
        String reason
) {
}
