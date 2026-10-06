package com.platform.ecom.catalog.application;

import com.platform.ecom.catalog.domain.Product;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CatalogEnrichmentTool {

    public Map<String, String> extractSuggestedAttributes(Product product) {
        Map<String, String> attributes = new HashMap<>();
        String nameLower = product.getName().toLowerCase();

        if (nameLower.contains("wireless") || nameLower.contains("bluetooth")) {
            attributes.put("connectivity", "Wireless / Bluetooth");
        } else {
            attributes.put("connectivity", "Wired");
        }

        if (product.getCategory() != null) {
            attributes.put("primary_category", product.getCategory());
        } else {
            attributes.put("primary_category", "Uncategorized");
        }

        return attributes;
    }

    public int calculateCompletenessScore(Product product, Map<String, String> enrichedAttributes) {
        int score = 0;
        if (product.getName() != null && !product.getName().isBlank()) score += 40;
        if (product.getCategory() != null && !product.getCategory().isBlank()) score += 30;
        if (!enrichedAttributes.isEmpty()) score += 30;
        return score;
    }
}
