package com.platform.ecom.identity.application;

import com.platform.ecom.identity.domain.User;
import com.platform.ecom.identity.infrastructure.JwtService;
import com.platform.ecom.identity.infrastructure.UserRepository;
import com.platform.ecom.shared.Email;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthenticatedSession login(UUID tenantId, Email email, String rawPassword) {
        User user = userRepository.findByTenantIdAndEmail(tenantId, email.value())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            // Same exception, same message as "user not found" above —
            // deliberately. Returning a different error for "wrong email"
            // vs "wrong password" lets an attacker enumerate which emails
            // are registered; from outside this method both failures must
            // look identical.
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String token = jwtService.issueToken(user.getId(), user.getTenantId(), user.getRole().name());
        return new AuthenticatedSession(token, jwtService.expirationSeconds());
    }

    public record AuthenticatedSession(String accessToken, long expiresInSeconds) {
    }
}