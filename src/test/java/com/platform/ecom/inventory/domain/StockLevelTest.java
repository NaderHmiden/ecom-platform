package com.platform.ecom.inventory.domain;

import com.platform.ecom.shared.exception.InvariantViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StockLevelTest {

    private final UUID tenantId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @Test
    @DisplayName("Reserving stock decreases available and increases reserved")
    void reserveStockSuccess() {
        StockLevel stockLevel = new StockLevel(tenantId, productId, "SKU-100", 50);

        stockLevel.reserve(15);

        assertEquals(35, stockLevel.getAvailableQuantity());
        assertEquals(15, stockLevel.getReservedQuantity());
    }

    @Test
    @DisplayName("Reserving more than available stock throws InvariantViolationException")
    void reserveInsufficientStockThrows() {
        StockLevel stockLevel = new StockLevel(tenantId, productId, "SKU-100", 10);

        assertThrows(InvariantViolationException.class, () -> stockLevel.reserve(15));
    }
}
