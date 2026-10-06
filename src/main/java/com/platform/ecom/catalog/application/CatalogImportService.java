package com.platform.ecom.catalog.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.ecom.catalog.domain.Product;
import com.platform.ecom.catalog.infrastructure.ProductRepository;
import com.platform.ecom.shared.Money;
import com.platform.ecom.supplier.domain.RawSupplierRow;
import com.platform.ecom.supplier.domain.RowStatus;
import com.platform.ecom.supplier.infrastructure.RawSupplierRowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The concrete answer to "how does a normalized supplier row become a
 * catalog Product": catalog PULLS from supplier's already-normalized
 * data. This is only possible because catalog is allowed to depend on
 * supplier (Phase 2's dependency graph) — the reverse is forbidden.
 * supplier has no idea this class exists.
 */
@Service
public class CatalogImportService {

    // Supplier rows don't carry a currency column (SupplierRowValidator's
    // minimal required-field set doesn't include one) — hardcoded until a
    // real multi-currency need exists. Flagged, not hidden.
    private static final String DEFAULT_CURRENCY = "USD";
    private static final List<String> CORE_FIELDS = List.of("sku", "name", "price");

    private final RawSupplierRowRepository rowRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    public CatalogImportService(RawSupplierRowRepository rowRepository,
                                ProductRepository productRepository,
                                ObjectMapper objectMapper) {
        this.rowRepository = rowRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void importNormalizedRows(UUID tenantId, UUID importJobId) {
        List<RawSupplierRow> normalizedRows =
                rowRepository.findByImportJobIdAndStatus(importJobId, RowStatus.NORMALIZED);

        for (RawSupplierRow row : normalizedRows) {
            // Idempotency: if this row already produced a Product, skip
            // it rather than creating a duplicate. The DB's unique
            // constraint on (tenant_id, source_row_id) would also catch
            // this, but checking first avoids throwing on a harmless re-run.
            if (productRepository.findByTenantIdAndSourceRowId(tenantId, row.getId()).isPresent()) {
                continue;
            }
            createProductFromRow(tenantId, row);
        }
    }

    private void createProductFromRow(UUID tenantId, RawSupplierRow row) {
        Map<String, String> data = parseRawData(row.getRawDataJson());

        String sku = data.get("sku");
        String name = data.get("name");
        BigDecimal price = new BigDecimal(data.get("price"));

        // Everything besides sku/name/price becomes variant attributes —
        // the "attributes as opaque JSON" simplification from
        // ProductVariant's own class comment.
        Map<String, String> attributes = data.entrySet().stream()
                .filter(e -> !CORE_FIELDS.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Product product = Product.createWithInitialVariant(
                tenantId,
                name,
                null, // category: deferred — see Phase 6 design notes
                row.getId(),
                sku,
                Money.of(price, DEFAULT_CURRENCY),
                toJson(attributes)
        );

        productRepository.save(product);
    }

    private Map<String, String> parseRawData(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException("Corrupt raw row data", e);
        }
    }

    private String toJson(Map<String, String> attributes) {
        try {
            return objectMapper.writeValueAsString(attributes);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize attributes to JSON", e);
        }
    }
}