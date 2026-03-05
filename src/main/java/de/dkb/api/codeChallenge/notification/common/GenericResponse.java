package de.dkb.api.codeChallenge.notification.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timestamp", "traceId", "errorCode", "message", "data"})
@Schema(description = "Standard API response envelope")
public class GenericResponse<T> {

    public static final String UNKNOWN = "UNKNOWN";
    @Schema(description = "Response timestamp in ISO-8601 format", example = "2025-03-04T12:00:00Z")
    private final OffsetDateTime timestamp;

    @Schema(description = "Request trace ID for correlation and debugging")
    private final String traceId;

    @Schema(description = "Error code when request failed; null for success", example = "VAL-001", nullable = true)
    private final String errorCode;

    @Schema(description = "Human-readable message")
    private final String message;

    @Schema(description = "Response payload; null for error responses when no additional data", nullable = true)
    private final T data;

    private GenericResponse(String traceId, String errorCode, String message, T data) {
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        this.traceId = resolveTraceId(traceId);
        this.errorCode = errorCode;
        this.message = message;
        this.data = data;
    }

    public static <T> GenericResponse<T> success(String traceId, String message, T data) {
        return new GenericResponse<>(traceId, null, message, data);
    }

    public static <T> GenericResponse<T> error(String traceId, String errorCode, String message, T data) {
        return new GenericResponse<>(traceId, errorCode, message, data);
    }

    public static <T> GenericResponse<T> error(String traceId, String errorCode, String message) {
        return error(traceId, errorCode, message, null);
    }

    private static String resolveTraceId(String traceId) {
        return traceId != null && !traceId.isBlank() ? traceId : UNKNOWN;
    }
}