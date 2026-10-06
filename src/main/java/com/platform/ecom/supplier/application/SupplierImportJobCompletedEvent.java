package com.platform.ecom.supplier.application;
import java.util.UUID;
public record SupplierImportJobCompletedEvent(UUID tenantId, UUID importJobId) {
}
