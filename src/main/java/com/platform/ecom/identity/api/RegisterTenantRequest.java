package com.platform.ecom.identity.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterTenantRequest(
        @NotBlank String tenantName,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 8, message = "must be at least 8 characters") String adminPassword
) {
}