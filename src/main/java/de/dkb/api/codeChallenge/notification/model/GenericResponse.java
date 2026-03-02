package de.dkb.api.codeChallenge.notification.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenericResponse<T> {

    private LocalDateTime timestamp;
    private String service;
    private String traceId;
    private String path;
    private int status;
    private boolean success;
    private String errorCode;
    private String message;
    private T data;

    // =========================
    // SUCCESS RESPONSE
    // =========================
    public static <T> GenericResponse<T> success(
            String service,
            String traceId,
            String path,
            int status,
            String message,
            T data
    ) {
        return GenericResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .service(service)
                .traceId(traceId)
                .path(path)
                .status(status)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // =========================
    // ERROR RESPONSE (WITHOUT DATA)
    // =========================
    public static GenericResponse<Void> error(
            String service,
            String traceId,
            String path,
            int status,
            String errorCode,
            String message
    ) {
        return GenericResponse.<Void>builder()
                .timestamp(LocalDateTime.now())
                .service(service)
                .traceId(traceId)
                .path(path)
                .status(status)
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();
    }

    // =========================
    // ERROR RESPONSE (WITH DATA)
    // =========================
    public static <T> GenericResponse<T> error(
            String service,
            String traceId,
            String path,
            int status,
            String errorCode,
            String message,
            T data
    ) {
        return GenericResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .service(service)
                .traceId(traceId)
                .path(path)
                .status(status)
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .data(data)
                .build();
    }
}