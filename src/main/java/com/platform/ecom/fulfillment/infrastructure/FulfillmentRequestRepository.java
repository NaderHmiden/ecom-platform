package com.platform.ecom.fulfillment.infrastructure;

import com.platform.ecom.fulfillment.domain.FulfillmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FulfillmentRequestRepository extends JpaRepository<FulfillmentRequest, UUID> {
    Optional<FulfillmentRequest> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<FulfillmentRequest> findByOrderIdAndTenantId(UUID orderId, UUID tenantId);
}
