package com.platform.ecom.order.application;

import com.platform.ecom.order.domain.Order;
import com.platform.ecom.order.infrastructure.OrderRepository;
import com.platform.ecom.shared.Money;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public record CreateOrderItemParam(UUID productId, String sku, int quantity, BigDecimal unitPriceAmount, String currencyCode) {}

    @Transactional
    public Order createOrder(String externalOrderId, List<CreateOrderItemParam> itemParams) {
        UUID tenantId = currentTenantId();

        // Idempotency Check
        return orderRepository.findByTenantIdAndExternalOrderId(tenantId, externalOrderId)
                .orElseGet(() -> {
                    String currency = itemParams.isEmpty() ? "USD" : itemParams.getFirst().currencyCode();
                    Order order = new Order(tenantId, externalOrderId, currency);
                    for (CreateOrderItemParam param : itemParams) {
                        Money price = Money.of(param.unitPriceAmount(), param.currencyCode());
                        order.addItem(param.productId(), param.sku(), param.quantity(), price);
                    }
                    return orderRepository.save(order);
                });
    }

    @Transactional
    public void markProcessing(UUID orderId) {
        UUID tenantId = currentTenantId();
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.markProcessing();
        orderRepository.save(order);
    }

    @Transactional
    public void markCompleted(UUID orderId) {
        UUID tenantId = currentTenantId();
        Order order = orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        order.markCompleted();
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrder(UUID orderId) {
        UUID tenantId = currentTenantId();
        return orderRepository.findByIdAndTenantId(orderId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private UUID currentTenantId() {
        return TenantContext.current()
                .map(TenantId::value)
                .orElseThrow(() -> new IllegalStateException("Tenant context required for order operations"));
    }
}
