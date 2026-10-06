package com.platform.ecom.supplier.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Deterministic, pure validation of a single raw supplier row's structural
 * shape. No LLM, no I/O — exactly the kind of check the original brief
 * said should never be handed to an AI model: fixed rules a computer can
 * check reliably and instantly.
 *
 * The required-field set here (sku/name/price) is a deliberate placeholder,
 * not a final schema — Phase 6 defines the real Product/Attribute model.
 * This exists to prove the pipeline mechanics (parse -> validate ->
 * persist outcome -> counters -> idempotent re-import), not to be the
 * final word on what makes a product valid.
 */
public final class SupplierRowValidator {

    private static final List<String> REQUIRED_FIELDS = List.of("sku", "name", "price");

    private SupplierRowValidator() {
    }

    public static ValidationResult validate(Map<String, String> row) {
        List<String> errors = new ArrayList<>();

        for (String field : REQUIRED_FIELDS) {
            String value = row.get(field);
            if (value == null || value.isBlank()) {
                errors.add("Missing required field: " + field);
            }
        }

        String price = row.get("price");
        if (price != null && !price.isBlank()) {
            try {
                if (new BigDecimal(price).signum() <= 0) {
                    errors.add("price must be greater than zero");
                }
            } catch (NumberFormatException e) {
                errors.add("price is not a valid number: " + price);
            }
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }

    public record ValidationResult(boolean isValid, List<String> errors) {
        public static ValidationResult valid() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public String errorsAsString() {
            return String.join("; ", errors);
        }
    }
}