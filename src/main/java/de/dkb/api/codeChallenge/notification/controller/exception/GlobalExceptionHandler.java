package de.dkb.api.codeChallenge.notification.controller.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import de.dkb.api.codeChallenge.notification.common.GenericResponse;
import de.dkb.api.codeChallenge.notification.domain.NotificationType;
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
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_KEY = "traceId";
    public static final String UNKNOWN = "UNKNOWN";

    private final MeterRegistry meterRegistry;

    public GlobalExceptionHandler(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // Utility for resolving the TraceId
    private String resolveTraceId() {
        return Optional.ofNullable(MDC.get(TRACE_ID_KEY))
                .orElse(UNKNOWN);
    }

    private void incrementErrorCounter(String errorCode, int status) {
        meterRegistry.counter(
                "application_errors_total",
                "errorCode", errorCode,
                "status", String.valueOf(status)
        ).increment();
    }


    // Domain Exceptions

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<GenericResponse<Void>> handleNotificationException(
            NotificationException ex,
            HttpServletRequest request) {

        String traceId = resolveTraceId();
        String path = request.getRequestURI();
        ErrorCode error = ex.getErrorCode();

        incrementErrorCounter(error.getCode(), error.getHttpStatus().value());

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


    // Validation Exceptions (@Valid)

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


    // Invalid JSON / Enum / Payload

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse<Void>> handleInvalidPayload(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        String traceId = resolveTraceId();
        String path = request.getRequestURI();

        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        String userMessage = ErrorCode.VALIDATION_ERROR.getDefaultMessage();

        InvalidFormatException invalidFormat = findCause(ex, InvalidFormatException.class);

        if (invalidFormat != null) {

            Class<?> targetType = invalidFormat.getTargetType();

            if (targetType != null) {

                if (UUID.class.isAssignableFrom(targetType)) {
                    userMessage = "Invalid userId format. Expected a valid UUID (e.g. 550e8400-e29b-41d4-a716-446655440000)";
                    errorCode = ErrorCode.VALIDATION_ERROR;
                }

                else if (NotificationType.class.isAssignableFrom(targetType)) {
                    userMessage = "Invalid notification type. Must be one of: type1, type2, type3, type4, type5, type6";
                    errorCode = ErrorCode.INVALID_NOTIFICATION_TYPE;
                }
            }
        }

        incrementErrorCounter(
                errorCode.getCode(),
                errorCode.getHttpStatus().value()
        );

        log.warn("event=InvalidPayload path={} traceId={} error={}",
                path,
                traceId,
                ex.getMessage());

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .header(TRACE_ID_HEADER, traceId)
                .body(GenericResponse.error(
                        traceId,
                        errorCode.getCode(),
                        userMessage
                ));
    }

    private <T extends Throwable> T findCause(Throwable ex, Class<T> type) {
        Throwable current = ex;
        while (current != null) {
            if (type.isInstance(current)) {
                return type.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }


    // Fallback Exception

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