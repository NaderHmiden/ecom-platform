package com.platform.ecom.catalog.application;

import com.platform.ecom.catalog.domain.Product;
import com.platform.ecom.catalog.infrastructure.ProductRepository;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class CatalogEnrichmentOrchestrator {

    private final ProductRepository productRepository;
    private final CatalogEnrichmentTool enrichmentTool;

    public CatalogEnrichmentOrchestrator(ProductRepository productRepository, CatalogEnrichmentTool enrichmentTool) {
        this.productRepository = productRepository;
        this.enrichmentTool = enrichmentTool;
    }

    public record EnrichmentReport(UUID productId, Map<String, String> enrichedAttributes, int completenessScore) {
    }

    @Transactional(readOnly = true)
    public EnrichmentReport enrichProduct(UUID productId) {
        UUID tenantId = currentTenantId();
        Product product = productRepository.findByIdAndTenantId(productId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        Map<String, String> attributes = enrichmentTool.extractSuggestedAttributes(product);
        int score = enrichmentTool.calculateCompletenessScore(product, attributes);

        return new EnrichmentReport(product.getId(), attributes, score);
    }

    private UUID currentTenantId() {
        return TenantContext.current()
                .map(TenantId::value)
                .orElseThrow(() -> new IllegalStateException("Tenant context required for catalog enrichment"));
    }
}
