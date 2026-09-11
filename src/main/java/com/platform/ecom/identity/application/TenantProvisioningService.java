package com.platform.ecom.identity.application;

import com.platform.ecom.identity.domain.Role;
import com.platform.ecom.identity.domain.Tenant;
import com.platform.ecom.identity.domain.User;
import com.platform.ecom.identity.infrastructure.TenantRepository;
import com.platform.ecom.identity.infrastructure.UserRepository;
import com.platform.ecom.shared.Email;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates a new Tenant together with its first ADMIN user.
 *
 * NOTE ON AGGREGATE BOUNDARIES: this deliberately touches two aggregate
 * roots (Tenant and User) in one transaction. That's normally something to
 * avoid — aggregates are meant to be modified one at a time — but tenant
 * bootstrapping is a one-time, low-frequency operation with no concurrency
 * pressure, and a tenant with zero users is a state we never want to exist
 * even transiently. This is the bounded exception, not the pattern to reuse
 * elsewhere.
 */
@Service
public class TenantProvisioningService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantProvisioningService(TenantRepository tenantRepository,
                                     UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Tenant provisionTenant(String tenantName, Email adminEmail, String rawAdminPassword) {
        Tenant tenant = Tenant.create(tenantName);
        tenantRepository.save(tenant);

        String passwordHash = passwordEncoder.encode(rawAdminPassword);
        User admin = User.register(tenant.getId(), adminEmail, passwordHash, Role.ADMIN);
        userRepository.save(admin);

        return tenant;
    }
}