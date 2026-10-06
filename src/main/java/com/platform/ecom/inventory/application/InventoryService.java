package com.platform.ecom.inventory.application;

import com.platform.ecom.inventory.domain.StockLevel;
import com.platform.ecom.inventory.domain.StockReservation;
import com.platform.ecom.inventory.infrastructure.StockLevelRepository;
import com.platform.ecom.inventory.infrastructure.StockReservationRepository;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryService {

    private final StockLevelRepository stockLevelRepository;
    private final StockReservationRepository reservationRepository;

    public InventoryService(StockLevelRepository stockLevelRepository, StockReservationRepository reservationRepository) {
        this.stockLevelRepository = stockLevelRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public StockLevel initializeStock(UUID productId, String sku, int initialQuantity) {
        UUID tenantId = currentTenantId();
        StockLevel stockLevel = new StockLevel(tenantId, productId, sku, initialQuantity);
        return stockLevelRepository.save(stockLevel);
    }

    /**
     * Reserves stock for an order using PESSIMISTIC_WRITE lock query.
     */
    @Transactional
    public StockReservation reserveStock(UUID orderId, String sku, int quantity) {
        UUID tenantId = currentTenantId();
        StockLevel stockLevel = stockLevelRepository.findByTenantIdAndSkuPessimistic(tenantId, sku)
                .orElseThrow(() -> new ResourceNotFoundException("Stock level not found for SKU: " + sku));

        stockLevel.reserve(quantity);
        stockLevelRepository.save(stockLevel);

        StockReservation reservation = new StockReservation(tenantId, orderId, sku, quantity);
        return reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public StockLevel getStockLevel(String sku) {
        UUID tenantId = currentTenantId();
        return stockLevelRepository.findByTenantIdAndSku(tenantId, sku)
                .orElseThrow(() -> new ResourceNotFoundException("Stock level not found for SKU: " + sku));
    }

    private UUID currentTenantId() {
        return TenantContext.current()
                .map(TenantId::value)
                .orElseThrow(() -> new IllegalStateException("Tenant context required for inventory operations"));
    }
}
