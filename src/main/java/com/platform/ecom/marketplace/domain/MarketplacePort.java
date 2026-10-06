package com.platform.ecom.marketplace.domain;

import java.math.BigDecimal;
import java.util.Map;

public interface MarketplacePort {
    PublishResult publishListing(
            String title,
            String description,
            String bulletPoints,
            String keywords,
            BigDecimal priceAmount,
            String priceCurrency,
            String accountCredentials
    );
}
