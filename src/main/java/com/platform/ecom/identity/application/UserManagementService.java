package com.platform.ecom.identity.application;

import com.platform.ecom.identity.domain.Role;
import com.platform.ecom.identity.domain.User;
import com.platform.ecom.identity.infrastructure.UserRepository;
import com.platform.ecom.shared.Email;
import com.platform.ecom.shared.exception.InvariantViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User addUser(UUID tenantId, Email email, String rawPassword, Role role) {
        userRepository.findByTenantIdAndEmail(tenantId, email.value()).ifPresent(existing -> {
            throw new InvariantViolationException("A user with this email already exists for this tenant");
        });

        String passwordHash = passwordEncoder.encode(rawPassword);
        User user = User.register(tenantId, email, passwordHash, role);
        return userRepository.save(user);
    }

    /**
     * The invariant from Phase 1/2: a tenant must always keep at least one
     * ADMIN. Checked here, before the entity's own changeRole() runs,
     * because only this service can see every User for the tenant.
     */
    @Transactional
    public void changeRole(UUID tenantId, UUID userId, Role newRole) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getTenantId().equals(tenantId))
                .orElseThrow(() -> new InvariantViolationException("User not found in this tenant"));

        boolean isDemotingAnAdmin = user.getRole() == Role.ADMIN && newRole != Role.ADMIN;
        if (isDemotingAnAdmin) {
            long remainingAdmins = userRepository.countByTenantIdAndRole(tenantId, Role.ADMIN);
            if (remainingAdmins <= 1) {
                throw new InvariantViolationException("Cannot remove the last ADMIN of a tenant");
            }
        }

        user.changeRole(newRole);
        // No explicit save() — `user` is a managed entity inside this
        // @Transactional method; Hibernate's dirty checking persists the
        // change at commit. Calling save() here would work too, but would
        // wrongly suggest this is a detached entity being re-attached.
    }
}