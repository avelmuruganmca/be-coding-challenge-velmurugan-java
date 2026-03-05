package de.dkb.api.codeChallenge.notification.controller.exception;

import de.dkb.api.codeChallenge.notification.common.ApiMessages;
import lombok.Getter;
import org.springframework.http.HttpStatus;


import static de.dkb.api.codeChallenge.notification.common.ApiMessages.REQUEST_VALIDATION_FAILED;
import static de.dkb.api.codeChallenge.notification.common.ApiMessages.NOTIFICATION_TYPE_IS_NOT_SUPPORTED;
import static de.dkb.api.codeChallenge.notification.common.ApiMessages.UNEXPECTED_INTERNAL_SERVER_ERROR;
import static de.dkb.api.codeChallenge.notification.common.ApiMessages.USER_NOT_SUBSCRIBED_TO_NOTIFICATION_TYPE;


@Getter
public enum ErrorCode {

    VALIDATION_ERROR("VAL-001", REQUEST_VALIDATION_FAILED, HttpStatus.BAD_REQUEST),

    USER_NOT_FOUND("USR-404", ApiMessages.USER_NOT_FOUND, HttpStatus.NOT_FOUND),
    USER_NOT_SUBSCRIBED("USR-403", USER_NOT_SUBSCRIBED_TO_NOTIFICATION_TYPE, HttpStatus.FORBIDDEN),

    INVALID_NOTIFICATION_TYPE("NOT-400", ApiMessages.INVALID_NOTIFICATION_TYPE, HttpStatus.BAD_REQUEST),
    UNSUPPORTED_NOTIFICATION_TYPE("NOT-401", NOTIFICATION_TYPE_IS_NOT_SUPPORTED, HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR("SYS-500", UNEXPECTED_INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String defaultMessage, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }

}