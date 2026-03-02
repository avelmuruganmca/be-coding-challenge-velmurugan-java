package de.dkb.api.codeChallenge.notification.exception;

import de.dkb.api.codeChallenge.notification.config.ApplicationMetadata;
import de.dkb.api.codeChallenge.notification.model.GenericResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ApplicationMetadata metadata;

    public GlobalExceptionHandler(ApplicationMetadata metadata) {
        this.metadata = metadata;
    }

    // =========================
    // Domain Exceptions
    // =========================
    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<GenericResponse<Void>> handleNotificationException(
            NotificationException ex,
            HttpServletRequest request) {

        String traceId = MDC.get("traceId");
        String path = request.getRequestURI();

        ErrorCode error = ex.getErrorCode();

        log.warn("operation=DomainException status=Failed path={} errorCode={}",
                path,
                error.getCode());

        return ResponseEntity
                .status(error.getHttpStatus())
                .body(GenericResponse.error(
                        metadata.getServiceName(),
                        traceId,
                        path,
                        error.getHttpStatus().value(),
                        error.getCode(),
                        ex.getMessage()
                ));
    }

    // =========================
    // Validation Exceptions
    // =========================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null
                                ? error.getDefaultMessage()
                                : "Invalid value",
                        (existing, replacement) -> existing
                ));

        String traceId = MDC.get("traceId");
        String path = request.getRequestURI();

        log.warn("operation=Validation status=Failed path={} errors={}",
                path,
                errors);

        return ResponseEntity.badRequest().body(
                GenericResponse.error(
                        metadata.getServiceName(),
                        traceId,
                        path,
                        400,
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        "Request validation failed",
                        errors
                )
        );
    }

    // =========================
    // Fallback Exception
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        String traceId = MDC.get("traceId");
        String path = request.getRequestURI();

        log.error("operation=UnhandledException status=Failed path={} error={}",
                path,
                ex.getMessage(),
                ex);

        return ResponseEntity
                .internalServerError()
                .body(GenericResponse.error(
                        metadata.getServiceName(),
                        traceId,
                        path,
                        500,
                        "INTERNAL_SERVER_ERROR",
                        "Unexpected internal server error"
                ));
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse<Void>> handleInvalidEnum(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String traceId = MDC.get("traceId");
        String path = request.getRequestURI();

        log.warn("operation=InvalidPayload status=Failed path={} error={}",
                path,
                ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(GenericResponse.error(
                        metadata.getServiceName(),
                        traceId,
                        path,
                        ErrorCode.INVALID_NOTIFICATION_TYPE.getHttpStatus().value(),
                        ErrorCode.INVALID_NOTIFICATION_TYPE.getCode(),
                        "Invalid request payload"
                ));
    }
}