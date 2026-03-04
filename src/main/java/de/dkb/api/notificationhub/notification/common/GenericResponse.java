package de.dkb.api.notificationhub.notification.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {

    private final OffsetDateTime timestamp;
    private final String traceId;
    private final String errorCode;
    private final String message;
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
        return traceId != null && !traceId.isBlank() ? traceId : "UNKNOWN";
    }
}