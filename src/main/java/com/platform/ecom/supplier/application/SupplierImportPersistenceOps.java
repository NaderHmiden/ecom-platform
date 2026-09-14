package com.platform.ecom.supplier.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.ecom.supplier.domain.RawSupplierRow;
import com.platform.ecom.supplier.domain.SupplierImportJob;
import com.platform.ecom.supplier.infrastructure.RawSupplierRowRepository;
import com.platform.ecom.supplier.infrastructure.SupplierImportJobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Separate bean from SupplierImportService for the same proxy reason as
 * SupplierRowTransactionOps — but here the stakes are different: if the
 * caller triggered @Async processing BEFORE this method's transaction
 * actually committed, the background thread could start looking for rows
 * that, from its connection's point of view, don't exist yet. Keeping
 * this transactional work in its own bean means SupplierImportService's
 * call to it only returns after a real commit, making it safe to trigger
 * async work immediately afterward.
 */
@Service
public class SupplierImportPersistenceOps {

    private final SupplierImportJobRepository jobRepository;
    private final RawSupplierRowRepository rowRepository;
    private final ObjectMapper objectMapper;

    public SupplierImportPersistenceOps(SupplierImportJobRepository jobRepository,
                                        RawSupplierRowRepository rowRepository,
                                        ObjectMapper objectMapper) {
        this.jobRepository = jobRepository;
        this.rowRepository = rowRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SupplierImportJob persistJobAndRows(UUID tenantId,
                                               String fileName,
                                               String contentHash,
                                               List<Map<String, String>> parsedRows) {
        SupplierImportJob job = SupplierImportJob.create(tenantId, fileName, contentHash);
        jobRepository.save(job);

        int rowNumber = 0;
        for (Map<String, String> parsedRow : parsedRows) {
            rowNumber++;
            rowRepository.save(RawSupplierRow.create(tenantId, job.getId(), rowNumber, toJson(parsedRow)));
        }

        job.beginProcessing(parsedRows.size());
        jobRepository.save(job);
        return job;
    }

    private String toJson(Map<String, String> row) {
        try {
            return objectMapper.writeValueAsString(row);
        } catch (Exception e) {
            throw new IllegalStateException("Could not serialize row to JSON", e);
        }
    }
}