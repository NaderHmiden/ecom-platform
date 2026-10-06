package com.platform.ecom.experiment.infrastructure;

import com.platform.ecom.experiment.domain.MetricEvent;
import com.platform.ecom.experiment.domain.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MetricEventRepository extends JpaRepository<MetricEvent, UUID> {
    List<MetricEvent> findByExperimentIdAndTenantId(UUID experimentId, UUID tenantId);
    long countByExperimentIdAndVariantIdAndEventTypeAndTenantId(UUID experimentId, UUID variantId, MetricType eventType, UUID tenantId);
}
