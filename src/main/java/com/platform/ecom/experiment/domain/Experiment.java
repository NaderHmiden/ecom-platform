package com.platform.ecom.experiment.domain;

import com.platform.ecom.shared.exception.InvariantViolationException;
import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "experiments")
public class Experiment extends TenantScopedEntity {

    @Id
    private UUID id;

    @Column(name = "listing_id", nullable = false)
    private UUID listingId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperimentStatus status;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExperimentVariant> variants = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Experiment() {
    }

    public Experiment(UUID tenantId, UUID listingId, String name) {
        super(tenantId);
        if (name == null || name.isBlank()) {
            throw new InvariantViolationException("Experiment name must not be blank");
        }
        this.id = UUID.randomUUID();
        this.listingId = listingId;
        this.name = name;
        this.status = ExperimentStatus.DRAFT;
        this.createdAt = Instant.now();
    }

    public ExperimentVariant addVariant(UUID listingVersionId, String variantName, int trafficWeight) {
        if (status != ExperimentStatus.DRAFT) {
            throw new InvariantViolationException("Variants can only be added when experiment is in DRAFT state");
        }
        ExperimentVariant variant = new ExperimentVariant(getTenantId(), this, listingVersionId, variantName, trafficWeight);
        variants.add(variant);
        return variant;
    }

    public void start() {
        if (status != ExperimentStatus.DRAFT) {
            throw new InvariantViolationException("Can only start experiment from DRAFT state");
        }
        if (variants.size() < 2) {
            throw new InvariantViolationException("Experiment must have at least 2 variants before starting");
        }
        this.status = ExperimentStatus.RUNNING;
    }

    public void conclude() {
        if (status != ExperimentStatus.RUNNING) {
            throw new InvariantViolationException("Can only conclude experiment from RUNNING state");
        }
        this.status = ExperimentStatus.CONCLUDED;
    }

    public void cancel() {
        this.status = ExperimentStatus.CANCELLED;
    }

    public UUID getId() { return id; }
    public UUID getListingId() { return listingId; }
    public String getName() { return name; }
    public ExperimentStatus getStatus() { return status; }
    public List<ExperimentVariant> getVariants() { return List.copyOf(variants); }
    public Instant getCreatedAt() { return createdAt; }
}
