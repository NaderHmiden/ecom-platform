package com.platform.ecom.identity.api;

import com.platform.ecom.identity.application.AuthenticationService;
import com.platform.ecom.identity.application.TenantProvisioningService;
import com.platform.ecom.identity.domain.Tenant;
import com.platform.ecom.shared.Email;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TenantProvisioningService tenantProvisioningService;
    private final AuthenticationService authenticationService;

    public AuthController(TenantProvisioningService tenantProvisioningService,
                          AuthenticationService authenticationService) {
        this.tenantProvisioningService = tenantProvisioningService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register-tenant")
    public ResponseEntity<RegisterTenantResponse> registerTenant(@Valid @RequestBody RegisterTenantRequest request) {
        Tenant tenant = tenantProvisioningService.provisionTenant(
                request.tenantName(),
                new Email(request.adminEmail()),
                request.adminPassword());

        return ResponseEntity.ok(new RegisterTenantResponse(tenant.getId(), tenant.getName()));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthenticationService.AuthenticatedSession session = authenticationService.login(
                request.tenantId(),
                new Email(request.email()),
                request.password());

        return ResponseEntity.ok(new LoginResponse(session.accessToken(), session.expiresInSeconds()));
    }
}