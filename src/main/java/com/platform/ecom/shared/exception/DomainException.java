package com.platform.ecom.shared.exception;

/**
 * Base type for exceptions raised by domain logic (invariant violations,
 * illegal state transitions). Distinct from framework/infrastructure
 * exceptions so a global handler can map these to a consistent API error
 * shape (Phase 4, Step 5) without catching bare RuntimeException.
 */
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}