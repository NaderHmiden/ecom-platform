package com.platform.ecom.order.infrastructure;

import com.platform.ecom.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Order> findByTenantIdAndExternalOrderId(UUID tenantId, String externalOrderId);
    List<Order> findByTenantId(UUID tenantId);
}
