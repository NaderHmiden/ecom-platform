package com.platform.ecom.experiment.api;

import com.platform.ecom.experiment.application.ExperimentService;
import com.platform.ecom.experiment.domain.Experiment;
import com.platform.ecom.experiment.domain.ExperimentVariant;
import com.platform.ecom.experiment.domain.MetricEvent;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/experiments")
public class ExperimentController {

    private final ExperimentService experimentService;

    public ExperimentController(ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Experiment> createExperiment(@Valid @RequestBody CreateExperimentRequest request) {
        Experiment experiment = experimentService.createExperiment(request.listingId(), request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(experiment);
    }

    @PostMapping("/{experimentId}/variants")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<ExperimentVariant> addVariant(@PathVariable UUID experimentId,
                                                         @Valid @RequestBody AddVariantRequest request) {
        ExperimentVariant variant = experimentService.addVariant(
                experimentId,
                request.listingVersionId(),
                request.variantName(),
                request.trafficWeight() > 0 ? request.trafficWeight() : 50
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(variant);
    }

    @PostMapping("/{experimentId}/start")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> startExperiment(@PathVariable UUID experimentId) {
        experimentService.startExperiment(experimentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{experimentId}/events")
    public ResponseEntity<MetricEvent> recordMetric(@PathVariable UUID experimentId,
                                                     @Valid @RequestBody RecordMetricRequest request) {
        MetricEvent event = experimentService.recordMetricEvent(
                experimentId,
                request.variantId(),
                request.eventType(),
                request.value()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(event);
    }

    @GetMapping("/{experimentId}/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'REVIEWER')")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable UUID experimentId) {
        Map<String, Object> stats = experimentService.calculateExperimentStats(experimentId);
        return ResponseEntity.ok(stats);
    }
}
