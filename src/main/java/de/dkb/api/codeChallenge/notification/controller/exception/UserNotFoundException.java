package de.dkb.api.codeChallenge.notification.controller.exception;

import static de.dkb.api.codeChallenge.notification.common.ApiMessages.NOT_FOUND;
import static de.dkb.api.codeChallenge.notification.common.ApiMessages.USER_WITH_ID;

public class UserNotFoundException extends NotificationException {



    public UserNotFoundException(String userId) {
        super(ErrorCode.USER_NOT_FOUND,
                USER_WITH_ID + userId + NOT_FOUND);
    }
}