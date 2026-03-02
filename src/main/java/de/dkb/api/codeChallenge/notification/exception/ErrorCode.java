package de.dkb.api.codeChallenge.notification.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // =========================
    // Validation
    // =========================
    VALIDATION_ERROR("VAL-001", "Request validation failed", HttpStatus.BAD_REQUEST),

    // =========================
    // User Errors
    // =========================
    USER_NOT_FOUND("USR-404", "User not found", HttpStatus.NOT_FOUND),
    USER_NOT_SUBSCRIBED("USR-403", "User is not subscribed to this notification type", HttpStatus.FORBIDDEN),

    // =========================
    // Notification Errors
    // =========================
    INVALID_NOTIFICATION_TYPE("NOT-400", "Invalid notification type", HttpStatus.BAD_REQUEST),

    // =========================
    // System Errors
    // =========================
    INTERNAL_SERVER_ERROR("SYS-500", "Unexpected internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

}