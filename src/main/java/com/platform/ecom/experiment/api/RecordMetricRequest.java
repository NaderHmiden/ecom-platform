package com.platform.ecom.experiment.api;

import com.platform.ecom.experiment.domain.MetricType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record RecordMetricRequest(
        @NotNull UUID variantId,
        @NotNull MetricType eventType,
        BigDecimal value
) {
}
