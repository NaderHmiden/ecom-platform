package com.platform.ecom.shared;

import com.platform.ecom.shared.exception.InvariantViolationException;
import java.util.UUID;

public record TenantId(UUID value) {
    public TenantId {
        if (value == null) {
            throw new InvariantViolationException("TenantId must not be null");
        }
    }

    public static TenantId of(UUID value) {
        return new TenantId(value);
    }
}