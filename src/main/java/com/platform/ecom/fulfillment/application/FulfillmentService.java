package com.platform.ecom.fulfillment.application;

import com.platform.ecom.fulfillment.domain.FulfillmentRequest;
import com.platform.ecom.fulfillment.infrastructure.FulfillmentRequestRepository;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FulfillmentService {

    private final FulfillmentRequestRepository repository;

    public FulfillmentService(FulfillmentRequestRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FulfillmentRequest createFulfillmentRequest(UUID orderId) {
        UUID tenantId = currentTenantId();
        FulfillmentRequest request = new FulfillmentRequest(tenantId, orderId);
        return repository.save(request);
    }

    @Transactional
    public void markAllocated(UUID fulfillmentRequestId) {
        UUID tenantId = currentTenantId();
        FulfillmentRequest request = repository.findByIdAndTenantId(fulfillmentRequestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment request not found: " + fulfillmentRequestId));
        request.markAllocated();
        repository.save(request);
    }

    @Transactional
    public void markShipped(UUID fulfillmentRequestId, String trackingNumber) {
        UUID tenantId = currentTenantId();
        FulfillmentRequest request = repository.findByIdAndTenantId(fulfillmentRequestId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment request not found: " + fulfillmentRequestId));
        request.markShipped(trackingNumber);
        repository.save(request);
    }

    private UUID currentTenantId() {
        return TenantContext.current()
                .map(TenantId::value)
                .orElseThrow(() -> new IllegalStateException("Tenant context required for fulfillment operations"));
    }
}
