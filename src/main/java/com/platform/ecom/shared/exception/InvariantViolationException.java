package com.platform.ecom.shared.exception;

public class InvariantViolationException extends DomainException {
    public InvariantViolationException(String message) {
        super(message);
    }
}