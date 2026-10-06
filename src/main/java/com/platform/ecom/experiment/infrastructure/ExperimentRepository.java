package com.platform.ecom.experiment.infrastructure;

import com.platform.ecom.experiment.domain.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, UUID> {
    Optional<Experiment> findByIdAndTenantId(UUID id, UUID tenantId);
    List<Experiment> findByTenantId(UUID tenantId);
    List<Experiment> findByListingIdAndTenantId(UUID listingId, UUID tenantId);
}
