package de.dkb.api.notificationhub.notification.exception;

import de.dkb.api.notificationhub.notification.domain.NotificationType;

public class UnsupportedNotificationTypeException extends NotificationException {

    public UnsupportedNotificationTypeException(NotificationType type) {
        super(ErrorCode.UNSUPPORTED_NOTIFICATION_TYPE, "Notification type '" + type + "' is not supported");
    }
}