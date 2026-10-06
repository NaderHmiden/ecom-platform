package com.platform.ecom.ai.domain;

import java.math.BigDecimal;
import java.util.Map;

public record GenerationResult(
        String rawOutput,
        Map<String, String> structuredFields,
        String modelName,
        String promptVersion,
        int inputTokens,
        int outputTokens,
        long latencyMs,
        BigDecimal estimatedCost
) {
}
