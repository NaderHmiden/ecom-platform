
        package com.platform.ecom.shared.api;

import com.platform.ecom.shared.api.ApiError;
import com.platform.ecom.shared.exception.AuthenticationFailedException;
import com.platform.ecom.shared.exception.DomainException;
import com.platform.ecom.shared.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Single place mapping exceptions to the API error shape. Deliberately NOT
 * a catch-all `catch (Exception e)` handler — each handler here is specific
 * to one exception type, so anything unexpected still surfaces as an
 * unhandled 500 with a real stack trace in logs, rather than being
 * silently flattened into a generic "something went wrong" response that
 * would hide bugs instead of surfacing them.
 *
 * Depends only on shared.exception types — this is why AuthenticationFailedException
 * lives in shared rather than in identity: this handler cannot import
 * identity's InvalidCredentialsException without violating the module
 * dependency rules from Phase 2/3 (shared must not depend on business
 * modules).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(DomainException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                "DOMAIN_RULE_VIOLATION",
                ex.getMessage(),
                List.of(),
                traceId(),
                Instant.now()
        ));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiError> handleAuthenticationFailed(AuthenticationFailedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiError(
                "AUTHENTICATION_FAILED",
                ex.getMessage(),
                List.of(),
                traceId(),
                Instant.now()
        ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                List.of(),
                traceId(),
                Instant.now()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(
                "VALIDATION_FAILED",
                "Request validation failed",
                details,
                traceId(),
                Instant.now()
        ));
    }

    private String traceId() {
        // A real correlation ID (propagated via MDC across the request)
        // lands here in Phase 17 (Observability). Random UUID for now so
        // the response shape is already final and nothing downstream needs
        // to change when that arrives.
        return UUID.randomUUID().toString();
    }
}
 
