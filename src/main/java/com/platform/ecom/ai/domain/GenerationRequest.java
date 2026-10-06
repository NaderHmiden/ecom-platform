package com.platform.ecom.ai.domain;

import java.util.Map;

public record GenerationRequest(
        String prompt,
        String systemInstruction,
        String modelName,
        String promptVersion,
        double temperature,
        int maxTokens,
        Map<String, String> jsonSchema
) {
    public static GenerationRequest of(String prompt, String systemInstruction, String promptVersion) {
        return new GenerationRequest(
                prompt,
                systemInstruction,
                "gpt-4o-mini",
                promptVersion,
                0.7,
                1000,
                Map.of()
        );
    }
}
