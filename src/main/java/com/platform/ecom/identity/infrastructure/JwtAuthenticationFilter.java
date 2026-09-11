package com.platform.ecom.identity.infrastructure;

import com.platform.ecom.identity.domain.Role;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        try {
            if (header != null && header.startsWith("Bearer ")) {
                Claims claims = jwtService.parseAndValidate(header.substring(7));

                UUID userId = UUID.fromString(claims.getSubject());
                UUID tenantId = UUID.fromString(claims.get("tenantId", String.class));
                Role role = Role.valueOf(claims.get("role", String.class));

                TenantContext.set(TenantId.of(tenantId));

                var authentication = new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException e) {
            // Invalid/expired/malformed token: leave SecurityContext empty
            // rather than reject here directly. Spring Security's own
            // authorization rules (SecurityConfig) then reject the request
            // as unauthenticated — one place decides "who can do what,"
            // not two.
            filterChain.doFilter(request, response);
        } finally {
            // MANDATORY. doFilter() above blocks until the entire downstream
            // chain — including the controller — has finished, so clearing
            // here happens after this request no longer needs the context,
            // and before this pooled thread could pick up an unrelated one.
            TenantContext.clear();
        }
    }
}