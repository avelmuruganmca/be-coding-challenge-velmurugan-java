package de.dkb.api.notificationhub.notification.exception;

public class UserNotSubscribedException extends NotificationException {

    public UserNotSubscribedException() {
        super(ErrorCode.USER_NOT_SUBSCRIBED);
    }
}