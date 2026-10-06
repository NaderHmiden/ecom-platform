package com.platform.ecom.listing.domain;

import java.util.List;

public record RuleSet(
        int minTitleLength,
        int maxTitleLength,
        int maxDescriptionLength,
        int maxBulletPointLength,
        boolean requireDescription,
        boolean requirePrice,
        List<String> bannedClaims
) {
    public static RuleSet defaultConfig() {
        return new RuleSet(
                5,
                200,
                2000,
                500,
                true,
                true,
                List.of("100% free shipping", "guaranteed miracle", "best seller #1", "risk free claim")
        );
    }
}
