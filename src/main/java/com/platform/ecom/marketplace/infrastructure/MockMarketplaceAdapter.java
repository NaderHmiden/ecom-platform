package com.platform.ecom.marketplace.infrastructure;

import com.platform.ecom.marketplace.domain.MarketplacePort;
import com.platform.ecom.marketplace.domain.PublishResult;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@Primary
public class MockMarketplaceAdapter implements MarketplacePort {

    @Override
    public PublishResult publishListing(String title,
                                         String description,
                                         String bulletPoints,
                                         String keywords,
                                         BigDecimal priceAmount,
                                         String priceCurrency,
                                         String accountCredentials) {
        if (title != null && title.toLowerCase().contains("simulated-failure")) {
            return PublishResult.failure("Simulated sandbox publish rejection: invalid attributes");
        }
        String mockExternalId = "MOCK-MP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PublishResult.success(mockExternalId);
    }
}
