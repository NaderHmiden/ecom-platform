package com.platform.ecom.shared.exception;

/**
 * Base type for "who you say you are couldn't be verified" failures.
 * Deliberately NOT a DomainException subtype — those map to 400 Bad
 * Request (a business rule was violated); this maps to 401 Unauthorized
 * (identity wasn't established at all). Conflating the two would give
 * wrong HTTP semantics to API clients.
 */
public abstract class AuthenticationFailedException extends RuntimeException {
    protected AuthenticationFailedException(String message) {
        super(message);
    }
}