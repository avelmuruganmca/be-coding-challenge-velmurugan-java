package de.dkb.api.notificationhub.exception;

import de.dkb.api.notificationhub.domain.NotificationType;

public class UnsupportedNotificationTypeException extends NotificationException {

    public UnsupportedNotificationTypeException(NotificationType type) {
        super(ErrorCode.UNSUPPORTED_NOTIFICATION_TYPE, "Notification type '" + type + "' is not supported");
    }
}