package com.platform.ecom.ai.infrastructure;

import com.platform.ecom.ai.domain.GenerationPort;
import com.platform.ecom.ai.domain.GenerationRequest;
import com.platform.ecom.ai.domain.GenerationResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@Primary
public class MockGenerationAdapter implements GenerationPort {

    @Override
    public GenerationResult generate(GenerationRequest request) {
        long startTime = System.currentTimeMillis();

        String prompt = request.prompt();
        String productName = extractValue(prompt, "Product:", "Product Name:");
        if (productName == null || productName.isBlank()) {
            productName = "Premium Optimized Product";
        }

        Map<String, String> structured = new HashMap<>();
        structured.put("title", "Optimized " + productName + " - High Quality Listing");
        structured.put("description", "Discover the amazing features of " + productName + ". Engineered for durability, high performance, and customer satisfaction.");
        structured.put("bulletPoints", "Premium grade material; Ergonomic & durable design; Easy setup and maintenance");
        structured.put("keywords", productName.toLowerCase() + ", ecommerce, top quality, trending");

        long latency = System.currentTimeMillis() - startTime + 5; // simulated execution time

        return new GenerationResult(
                structured.toString(),
                structured,
                "mock-llm-v1",
                request.promptVersion(),
                150,
                200,
                latency,
                new BigDecimal("0.0005")
        );
    }

    private String extractValue(String text, String... markers) {
        for (String marker : markers) {
            int idx = text.indexOf(marker);
            if (idx != -1) {
                int start = idx + marker.length();
                int end = text.indexOf("\n", start);
                if (end == -1) end = text.length();
                return text.substring(start, end).trim();
            }
        }
        return null;
    }
}
