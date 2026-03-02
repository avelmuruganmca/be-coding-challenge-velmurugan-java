package de.dkb.api.codeChallenge.notification.exception;

public class UserNotFoundException extends NotificationException {

    public UserNotFoundException(String userId) {
        super(ErrorCode.USER_NOT_FOUND,
                "User with id " + userId + " not found");
    }
}