package com.platform.ecom.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiError(String code, String message, List<String> details, String traceId, Instant timestamp) {
}