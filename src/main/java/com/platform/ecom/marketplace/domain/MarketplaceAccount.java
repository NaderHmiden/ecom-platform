package com.platform.ecom.marketplace.domain;

import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "marketplace_accounts")
public class MarketplaceAccount extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "marketplace_name", nullable = false)
    private String marketplaceName;

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "credentials_json", nullable = false)
    private String credentialsJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MarketplaceAccount() {
    }

    public MarketplaceAccount(UUID tenantId, String marketplaceName, String accountName, String credentialsJson) {
        super(tenantId);
        this.id = UUID.randomUUID();
        this.marketplaceName = marketplaceName;
        this.accountName = accountName;
        this.credentialsJson = credentialsJson != null ? credentialsJson : "{}";
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getMarketplaceName() { return marketplaceName; }
    public String getAccountName() { return accountName; }
    public String getCredentialsJson() { return credentialsJson; }
    public Instant getCreatedAt() { return createdAt; }
}
