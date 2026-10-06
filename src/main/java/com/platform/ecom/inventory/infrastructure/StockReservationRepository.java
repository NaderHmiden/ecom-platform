package com.platform.ecom.inventory.infrastructure;

import com.platform.ecom.inventory.domain.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    List<StockReservation> findByOrderIdAndTenantId(UUID orderId, UUID tenantId);
}
