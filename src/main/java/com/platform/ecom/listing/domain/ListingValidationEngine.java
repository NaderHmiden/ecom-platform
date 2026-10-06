package com.platform.ecom.listing.domain;

import java.util.ArrayList;
import java.util.List;

public final class ListingValidationEngine {

    private ListingValidationEngine() {
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

    public static ValidationResult validate(ListingVersion version, RuleSet ruleSet) {
        List<String> errors = new ArrayList<>();

        if (version.getTitle() == null || version.getTitle().isBlank()) {
            errors.add("Title is required");
        } else {
            int len = version.getTitle().trim().length();
            if (len < ruleSet.minTitleLength()) {
                errors.add("Title must be at least " + ruleSet.minTitleLength() + " characters long");
            }
            if (len > ruleSet.maxTitleLength()) {
                errors.add("Title must not exceed " + ruleSet.maxTitleLength() + " characters");
            }
        }

        if (ruleSet.requireDescription()) {
            if (version.getDescription() == null || version.getDescription().isBlank()) {
                errors.add("Description is required");
            } else if (version.getDescription().length() > ruleSet.maxDescriptionLength()) {
                errors.add("Description must not exceed " + ruleSet.maxDescriptionLength() + " characters");
            }
        }

        if (ruleSet.requirePrice() && version.getPrice() == null) {
            errors.add("Price is required");
        }

        String combinedContent = (
                (version.getTitle() != null ? version.getTitle() : "") + " " +
                (version.getDescription() != null ? version.getDescription() : "") + " " +
                (version.getBulletPoints() != null ? version.getBulletPoints() : "")
        ).toLowerCase();

        for (String claim : ruleSet.bannedClaims()) {
            if (combinedContent.contains(claim.toLowerCase())) {
                errors.add("Contains banned claim: " + claim);
            }
        }

        return errors.isEmpty() ? ValidationResult.valid() : ValidationResult.invalid(errors);
    }
}
