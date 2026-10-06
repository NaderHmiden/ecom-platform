package com.platform.ecom.experiment.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateExperimentRequest(
        @NotNull UUID listingId,
        @NotBlank String name
) {
}
