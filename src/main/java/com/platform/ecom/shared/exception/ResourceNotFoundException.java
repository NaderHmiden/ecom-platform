package com.platform.ecom.shared.exception;

/**
 * Used for both "this doesn't exist" AND "this exists but belongs to a
 * different tenant." Deliberately the same exception, same message, same
 * HTTP status (404) for both cases — telling a caller "that resource
 * belongs to someone else" (e.g. via a 403) confirms the resource exists,
 * which is itself an information leak across the tenant boundary ADR-002
 * asked us to protect.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}