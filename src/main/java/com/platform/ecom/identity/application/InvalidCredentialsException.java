package com.platform.ecom.identity.application;

import com.platform.ecom.shared.exception.AuthenticationFailedException;

public class InvalidCredentialsException extends AuthenticationFailedException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}