package com.platform.ecom.shared;

import com.platform.ecom.shared.exception.InvariantViolationException;
import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount == null || amount.signum() < 0) {
            throw new InvariantViolationException("Money amount must be zero or positive");
        }
        if (currency == null) {
            throw new InvariantViolationException("Money must have a currency");
        }
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }
}