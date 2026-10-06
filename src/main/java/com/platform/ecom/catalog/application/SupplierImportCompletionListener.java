package com.platform.ecom.catalog.application;

import com.platform.ecom.supplier.application.SupplierImportJobCompletedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SupplierImportCompletionListener {

    private final CatalogImportService catalogImportService;

    public SupplierImportCompletionListener(CatalogImportService catalogImportService) {
        this.catalogImportService = catalogImportService;
    }

    @EventListener
    public void onImportJobCompleted(SupplierImportJobCompletedEvent event) {
        catalogImportService.importNormalizedRows(event.tenantId(), event.importJobId());
    }
}