package de.dkb.api.codeChallenge.notification.controller.exception;

public class UserNotSubscribedException extends NotificationException {

    public UserNotSubscribedException() {
        super(ErrorCode.USER_NOT_SUBSCRIBED);
    }
}