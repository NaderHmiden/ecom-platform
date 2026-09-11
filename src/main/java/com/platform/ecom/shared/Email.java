package com.platform.ecom.shared;

import com.platform.ecom.shared.exception.InvariantViolationException;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern SIMPLE_EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (value == null || !SIMPLE_EMAIL_PATTERN.matcher(value).matches()) {
            throw new InvariantViolationException("Invalid email address: " + value);
        }
        value = value.trim().toLowerCase();
    }
}