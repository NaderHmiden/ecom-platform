package com.platform.ecom.experiment.domain;

import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "metric_events")
public class MetricEvent extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "experiment_id", nullable = false)
    private UUID experimentId;

    @Column(name = "variant_id", nullable = false)
    private UUID variantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private MetricType eventType;

    @Column(name = "event_value", nullable = false)
    private BigDecimal eventValue;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    protected MetricEvent() {
    }

    public MetricEvent(UUID tenantId, UUID experimentId, UUID variantId, MetricType eventType, BigDecimal eventValue) {
        super(tenantId);
        this.id = UUID.randomUUID();
        this.experimentId = experimentId;
        this.variantId = variantId;
        this.eventType = eventType;
        this.eventValue = eventValue != null ? eventValue : BigDecimal.ONE;
        this.recordedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getExperimentId() { return experimentId; }
    public UUID getVariantId() { return variantId; }
    public MetricType getEventType() { return eventType; }
    public BigDecimal getEventValue() { return eventValue; }
    public Instant getRecordedAt() { return recordedAt; }
}
