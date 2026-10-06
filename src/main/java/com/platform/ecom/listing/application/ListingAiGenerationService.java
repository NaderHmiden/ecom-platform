package com.platform.ecom.listing.application;

import com.platform.ecom.ai.domain.GenerationPort;
import com.platform.ecom.ai.domain.GenerationRequest;
import com.platform.ecom.ai.domain.GenerationResult;
import com.platform.ecom.catalog.domain.Product;
import com.platform.ecom.catalog.infrastructure.ProductRepository;
import com.platform.ecom.listing.domain.ListingValidationEngine;
import com.platform.ecom.listing.domain.ListingVersion;
import com.platform.ecom.listing.domain.RuleSet;
import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class ListingAiGenerationService {

    private final GenerationPort generationPort;
    private final ListingTransactionOps transactionOps;
    private final ProductRepository productRepository;

    public ListingAiGenerationService(GenerationPort generationPort,
                                      ListingTransactionOps transactionOps,
                                      ProductRepository productRepository) {
        this.generationPort = generationPort;
        this.transactionOps = transactionOps;
        this.productRepository = productRepository;
    }

    public ListingVersion generateListingVersion(UUID listingId, UUID productId, String createdBy) {
        UUID tenantId = currentTenantId();
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        String prompt = "Generate marketplace listing copy.\nProduct: " + product.getName() +
                "\nCategory: " + (product.getCategory() != null ? product.getCategory() : "General");
        String systemInstruction = "You are an expert ecommerce copywriter producing structured listing titles, descriptions, and keywords.";

        GenerationRequest request = GenerationRequest.of(prompt, systemInstruction, "v1.0");
        GenerationResult result = generationPort.generate(request);

        Map<String, String> fields = result.structuredFields();
        String title = fields.getOrDefault("title", product.getName());
        String description = fields.getOrDefault("description", "High quality " + product.getName());
        String bulletPoints = fields.getOrDefault("bulletPoints", "High quality material; Long lasting");
        String keywords = fields.getOrDefault("keywords", product.getName().toLowerCase());

        Money price = product.getVariants().isEmpty() ? null : product.getVariants().getFirst().getPrice();

        ListingVersion version = transactionOps.addNextVersion(
                tenantId,
                listingId,
                title,
                description,
                bulletPoints,
                keywords,
                price,
                createdBy != null ? createdBy : "ai-generator"
        );

        transactionOps.validateAndSubmit(tenantId, listingId, version.getId(), RuleSet.defaultConfig());
        return version;
    }

    private UUID currentTenantId() {
        return TenantContext.current()
                .map(TenantId::value)
                .orElseThrow(() -> new IllegalStateException("Tenant context required for AI generation"));
    }
}
