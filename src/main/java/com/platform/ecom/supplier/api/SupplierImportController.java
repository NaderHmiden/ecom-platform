package com.platform.ecom.supplier.api;

import com.platform.ecom.shared.TenantContext;
import com.platform.ecom.supplier.application.SupplierImportService;
import com.platform.ecom.supplier.domain.SupplierImportJob;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/supplier/imports")
public class SupplierImportController {

    private final SupplierImportService importService;

    public SupplierImportController(SupplierImportService importService) {
        this.importService = importService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ImportJobResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        UUID tenantId = TenantContext.currentOrThrow().value();

        SupplierImportJob job = importService.importFile(tenantId, file.getOriginalFilename(), file.getBytes());

        // 202 Accepted, not 200/201: by the time this returns, row
        // validation is still running on a background thread (Step 4).
        // The Location header tells the client where to poll for the
        // outcome — the response body's status field reflects whatever
        // was true at THIS instant, which will usually be PROCESSING.
        return ResponseEntity
                .accepted()
                .location(URI.create("/api/supplier/imports/" + job.getId()))
                .body(ImportJobResponse.from(job));
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ImportJobResponse> getJob(@PathVariable UUID jobId) {
        UUID tenantId = TenantContext.currentOrThrow().value();
        SupplierImportJob job = importService.getJob(tenantId, jobId);
        return ResponseEntity.ok(ImportJobResponse.from(job));
    }
}