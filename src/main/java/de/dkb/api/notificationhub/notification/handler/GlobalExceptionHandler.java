package de.dkb.api.notificationhub.notification.handler;

import de.dkb.api.notificationhub.notification.common.GenericResponse;
import de.dkb.api.notificationhub.notification.exception.ErrorCode;
import de.dkb.api.notificationhub.notification.exception.NotificationException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";

    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // =========================================================
    // Utility
    // =========================================================
    private String resolveTraceId() {
        return Optional.ofNullable(MDC.get(TRACE_ID_KEY))
                .orElse("UNKNOWN");
    }

    private void incrementErrorCounter(String errorCode, int status) {
        Counter.builder("application.errors.total")
                .description("Total number of application errors")
                .tag("errorCode", errorCode)
                .tag("status", String.valueOf(status))
                .register(meterRegistry)
                .increment();
    }

    // =========================================================
    // Domain Exceptions
    // =========================================================
    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<GenericResponse<Void>> handleNotificationException(
            NotificationException ex,
            HttpServletRequest request) {

        String traceId = resolveTraceId();
        String path = request.getRequestURI();
        ErrorCode error = ex.getErrorCode();

        incrementErrorCounter(
                error.getCode(),
                error.getHttpStatus().value()
        );

        log.warn("event=DomainException path={} traceId={} errorCode={}",
                path,
                traceId,
                error.getCode());

        return ResponseEntity
                .status(error.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(GenericResponse.error(
                        traceId,
                        error.getCode(),
                        ex.getMessage()
                ));
    }

    // =========================================================
    // Validation Exceptions (@Valid)
    // =========================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String traceId = resolveTraceId();
        String path = request.getRequestURI();

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Optional.ofNullable(error.getDefaultMessage())
                                .orElse("Invalid value"),
                        (existing, replacement) -> existing
                ));

        incrementErrorCounter(
                ErrorCode.VALIDATION_ERROR.getCode(),
                400
        );

        log.warn("event=ValidationFailure path={} traceId={} errors={}",
                path,
                traceId,
                errors);

        return ResponseEntity
                .badRequest()
                .header(TRACE_ID_HEADER, traceId)
                .body(GenericResponse.error(
                        traceId,
                        ErrorCode.VALIDATION_ERROR.getCode(),
                        ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                        errors
                ));
    }

    // =========================================================
    // Invalid JSON / Enum / Payload
    // =========================================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse<Void>> handleInvalidPayload(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String traceId = resolveTraceId();
        String path = request.getRequestURI();

        incrementErrorCounter(
                ErrorCode.INVALID_NOTIFICATION_TYPE.getCode(),
                400
        );

        log.warn("event=InvalidPayload path={} traceId={} error={}",
                path,
                traceId,
                ex.getMessage());

        return ResponseEntity
                .badRequest()
                .header(TRACE_ID_HEADER, traceId)
                .body(GenericResponse.error(
                        traceId,
                        ErrorCode.INVALID_NOTIFICATION_TYPE.getCode(),
                        ErrorCode.INVALID_NOTIFICATION_TYPE.getDefaultMessage()
                ));
    }

    // =========================================================
    // Fallback Exception (LAST)
    // =========================================================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        String traceId = resolveTraceId();
        String path = request.getRequestURI();

        incrementErrorCounter(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                500
        );

        log.error("event=UnhandledException path={} traceId={} error={}",
                path,
                traceId,
                ex.getMessage(),
                ex);

        return ResponseEntity
                .internalServerError()
                .header(TRACE_ID_HEADER, traceId)
                .body(GenericResponse.error(
                        traceId,
                        ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                        ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage()
                ));
    }
}