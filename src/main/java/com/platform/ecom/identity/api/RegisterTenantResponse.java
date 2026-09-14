package com.platform.ecom.identity.api;

import java.util.UUID;

public record RegisterTenantResponse(UUID tenantId, String tenantName) {
}