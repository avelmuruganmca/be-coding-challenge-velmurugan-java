package de.dkb.api.codeChallenge.notification.controller.exception;

import de.dkb.api.codeChallenge.notification.domain.NotificationType;

import static de.dkb.api.codeChallenge.notification.common.ApiMessages.IS_NOT_SUPPORTED;
import static de.dkb.api.codeChallenge.notification.common.ApiMessages.NOTIFICATION_TYPE;

public class UnsupportedNotificationTypeException extends NotificationException {

    public UnsupportedNotificationTypeException(NotificationType type) {
        super(ErrorCode.UNSUPPORTED_NOTIFICATION_TYPE, NOTIFICATION_TYPE + type + IS_NOT_SUPPORTED);
    }
}