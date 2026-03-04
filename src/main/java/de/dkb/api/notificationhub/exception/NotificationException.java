package de.dkb.api.notificationhub.exception;

import lombok.Getter;

@Getter
public class NotificationException extends RuntimeException {

    private final ErrorCode errorCode;

    public NotificationException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public NotificationException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

}