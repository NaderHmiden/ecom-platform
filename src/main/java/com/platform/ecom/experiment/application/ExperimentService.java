package com.platform.ecom.experiment.application;

import com.platform.ecom.experiment.domain.*;
import com.platform.ecom.experiment.infrastructure.ExperimentRepository;
import com.platform.ecom.experiment.infrastructure.MetricEventRepository;
import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.shared.TenantId;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ExperimentService {

    private final ExperimentRepository experimentRepository;
    private final MetricEventRepository metricEventRepository;

    public ExperimentService(ExperimentRepository experimentRepository, MetricEventRepository metricEventRepository) {
        this.experimentRepository = experimentRepository;
        this.metricEventRepository = metricEventRepository;
    }

    @Transactional
    public Experiment createExperiment(UUID listingId, String name) {
        UUID tenantId = currentTenantId();
        Experiment experiment = new Experiment(tenantId, listingId, name);
        return experimentRepository.save(experiment);
    }

    @Transactional
    public ExperimentVariant addVariant(UUID experimentId, UUID listingVersionId, String variantName, int trafficWeight) {
        UUID tenantId = currentTenantId();
        Experiment experiment = experimentRepository.findByIdAndTenantId(experimentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment not found: " + experimentId));
        ExperimentVariant variant = experiment.addVariant(listingVersionId, variantName, trafficWeight);
        experimentRepository.save(experiment);
        return variant;
    }

    @Transactional
    public void startExperiment(UUID experimentId) {
        UUID tenantId = currentTenantId();
        Experiment experiment = experimentRepository.findByIdAndTenantId(experimentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment not found: " + experimentId));
        experiment.start();
        experimentRepository.save(experiment);
    }

    @Transactional
    public MetricEvent recordMetricEvent(UUID experimentId, UUID variantId, MetricType eventType, BigDecimal value) {
        UUID tenantId = currentTenantId();
        MetricEvent event = new MetricEvent(tenantId, experimentId, variantId, eventType, value);
        return metricEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> calculateExperimentStats(UUID experimentId) {
        UUID tenantId = currentTenantId();
        Experiment experiment = experimentRepository.findByIdAndTenantId(experimentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Experiment not found: " + experimentId));

        Map<String, Object> stats = new HashMap<>();
        stats.put("experimentId", experiment.getId());
        stats.put("status", experiment.getStatus());

        Map<String, Object> variantStats = new HashMap<>();
        for (ExperimentVariant v : experiment.getVariants()) {
            long impressions = metricEventRepository.countByExperimentIdAndVariantIdAndEventTypeAndTenantId(
                    experimentId, v.getId(), MetricType.IMPRESSION, tenantId);
            long conversions = metricEventRepository.countByExperimentIdAndVariantIdAndEventTypeAndTenantId(
                    experimentId, v.getId(), MetricType.CONVERSION, tenantId);

            double conversionRate = impressions > 0 ? (double) conversions / impressions : 0.0;

            Map<String, Object> vStat = new HashMap<>();
            vStat.put("impressions", impressions);
            vStat.put("conversions", conversions);
            vStat.put("conversionRate", conversionRate);
            variantStats.put(v.getVariantName(), vStat);
        }
        stats.put("variantStats", variantStats);
        return stats;
    }

    private UUID currentTenantId() {
        return TenantContext.current()
                .map(TenantId::value)
                .orElseThrow(() -> new IllegalStateException("Tenant context required for experiment operations"));
    }
}
