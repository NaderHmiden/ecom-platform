package com.platform.ecom.experiment.domain;

import com.platform.ecom.shared.persistence.TenantScopedEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "experiment_variants")
public class ExperimentVariant extends TenantScopedEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "experiment_id", nullable = false)
    private Experiment experiment;

    @Column(name = "listing_version_id", nullable = false)
    private UUID listingVersionId;

    @Column(name = "variant_name", nullable = false)
    private String variantName;

    @Column(name = "traffic_weight", nullable = false)
    private int trafficWeight;

    protected ExperimentVariant() {
    }

    public ExperimentVariant(UUID tenantId, Experiment experiment, UUID listingVersionId, String variantName, int trafficWeight) {
        super(tenantId);
        this.id = UUID.randomUUID();
        this.experiment = experiment;
        this.listingVersionId = listingVersionId;
        this.variantName = variantName;
        this.trafficWeight = trafficWeight;
    }

    public UUID getId() { return id; }
    public Experiment getExperiment() { return experiment; }
    public UUID getListingVersionId() { return listingVersionId; }
    public String getVariantName() { return variantName; }
    public int getTrafficWeight() { return trafficWeight; }
}
