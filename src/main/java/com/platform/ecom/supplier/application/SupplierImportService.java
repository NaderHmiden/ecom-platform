package com.platform.ecom.supplier.application;

import com.platform.ecom.supplier.domain.SupplierImportJob;
import com.platform.ecom.supplier.infrastructure.SupplierCsvParser;
import com.platform.ecom.supplier.infrastructure.SupplierImportJobRepository;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SupplierImportService {

    private final SupplierImportJobRepository jobRepository;
    private final SupplierCsvParser csvParser;
    private final SupplierImportPersistenceOps persistenceOps;
    private final SupplierRowProcessor rowProcessor;

    public SupplierImportService(SupplierImportJobRepository jobRepository,
                                 SupplierCsvParser csvParser,
                                 SupplierImportPersistenceOps persistenceOps,
                                 SupplierRowProcessor rowProcessor) {
        this.jobRepository = jobRepository;
        this.csvParser = csvParser;
        this.persistenceOps = persistenceOps;
        this.rowProcessor = rowProcessor;
    }

    public SupplierImportJob importFile(UUID tenantId, String fileName, byte[] fileBytes) {
        String contentHash = sha256Hex(fileBytes);

        // Idempotent re-upload (Phase 0, journey J1): same tenant + same
        // file content = return the existing job, do not reprocess.
        //
        // KNOWN, ACCEPTED RACE: two identical uploads arriving at the same
        // instant could both pass this check before either commits. The
        // database's unique constraint on (tenant_id, content_hash) — see
        // V3__supplier.sql — prevents two jobs from actually existing, but
        // this code doesn't yet catch that constraint violation and
        // gracefully return the winner's job; the loser would currently
        // see a raw persistence exception. Flagged as a real follow-up,
        // not fixed now, so the phase doesn't balloon further.
        Optional<SupplierImportJob> existing = jobRepository.findByTenantIdAndContentHash(tenantId, contentHash);
        if (existing.isPresent()) {
            return existing.get();
        }

        List<Map<String, String>> parsedRows;
        try {
            parsedRows = csvParser.parse(new ByteArrayInputStream(fileBytes));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not parse file as CSV: " + fileName, e);
        }

        // persistJobAndRows() is a call through a real Spring proxy on a
        // DIFFERENT bean (SupplierImportPersistenceOps) — it only returns
        // once its @Transactional method has actually committed. That is
        // what makes it safe to trigger async processing on the very next
        // line: the rows the background thread is about to look for are
        // guaranteed to already be visible.
        SupplierImportJob job = persistenceOps.persistJobAndRows(tenantId, fileName, contentHash, parsedRows);

        rowProcessor.processJob(tenantId, job.getId());

        return job;
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * findById() is a direct load-by-primary-key — NOT guaranteed to be
     * covered by the automatic Hibernate tenant filter (see this step's
     * opening note). The explicit ownership check below is not optional
     * defense in depth here; for this call path, it's the only protection.
     * Same tenant, same message, whether the job doesn't exist at all or
     * belongs to someone else — see ResourceNotFoundException's own
     * comment for why that's deliberate.
     */
    public SupplierImportJob getJob(UUID tenantId, UUID jobId) {
        return jobRepository.findById(jobId)
                .filter(job -> job.getTenantId().equals(tenantId))
                .orElseThrow(() -> new ResourceNotFoundException("Import job not found: " + jobId));
    }
}