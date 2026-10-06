package com.platform.ecom.supplier.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.ecom.supplier.domain.RawSupplierRow;
import com.platform.ecom.supplier.domain.SupplierImportJob;
import com.platform.ecom.supplier.domain.SupplierRowValidator;
import com.platform.ecom.supplier.infrastructure.RawSupplierRowRepository;
import com.platform.ecom.supplier.infrastructure.SupplierImportJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Split into its OWN bean, separate from SupplierRowProcessor, specifically
 * to avoid Spring's self-invocation pitfall: @Transactional is implemented
 * via a proxy that wraps the bean from the outside. A call like
 * `this.processRow(row)` from within another method of the SAME class
 * never goes through that proxy — Spring silently runs it with no
 * transaction at all, and nobody gets an error telling them that happened.
 * Making this a distinct injected bean forces every call through the real
 * proxy.
 */
@Service
public class SupplierRowTransactionOps {

    private final SupplierImportJobRepository jobRepository;
    private final RawSupplierRowRepository rowRepository;
    private final ObjectMapper objectMapper;

    public SupplierRowTransactionOps(SupplierImportJobRepository jobRepository,
                                     RawSupplierRowRepository rowRepository,
                                     ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.rowRepository = rowRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processRow(RawSupplierRow row) {
        Map<String, String> data = parseRawData(row.getRawDataJson());
        SupplierRowValidator.ValidationResult result = SupplierRowValidator.validate(data);

        if (result.isValid()) {
            row.markNormalized();
            jobRepository.incrementSucceeded(row.getImportJobId());
        } else {
            row.markValidationFailed(result.errorsAsString());
            jobRepository.incrementFailed(row.getImportJobId());
        }
        rowRepository.save(row);
    }

    @Transactional
    public void finalizeJob(UUID jobId) {
        SupplierImportJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Import job disappeared mid-processing: " + jobId));

        // job's counters are fresh here — this is a brand-new read,
        // happening after every processRow() transaction above has
        // already committed its atomic increment (Phase 1: eventually
        // consistent counters, now finally consistent because processing
        // is done).
        job.finalizeWith(job.getSucceededRows(), job.getFailedRows());
        jobRepository.save(job);
    }

    private Map<String, String> parseRawData(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Corrupt raw row data — this should never happen since we wrote it ourselves", e);
        }
    }
}