package com.platform.ecom.ai.domain;

public interface GenerationPort {
    GenerationResult generate(GenerationRequest request);
}
