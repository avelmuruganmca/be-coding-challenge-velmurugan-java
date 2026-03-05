package de.dkb.api.codeChallenge.notification.common;

public final class ApiMessages {

    private ApiMessages() {}

    public static final String USER_REGISTERED_SUCCESS =
            "User registered successfully";

    public static final String NOTIFICATION_PROCESSED_SUCCESS =
            "Notification processed successfully";

    public static final String NOTIFICATION_STATUS_SENT =
            "SENT";

    public static final String INVALID_UUID_MESSAGE =
            "Invalid userId format. Expected a valid UUID (e.g. 550e8400-e29b-41d4-a716-446655440000)";

    public static final String NOTIFICATION_TYPE = "Notification type '";
    public static final String IS_INVALID_OR_NOT_SUPPORTED = "' is invalid or not supported";

    public static final String INVALID_NOTIFICATION_TYPE = "Invalid notification type.";

    public static final String REQUEST_VALIDATION_FAILED = "Request validation failed";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String USER_NOT_SUBSCRIBED_TO_NOTIFICATION_TYPE = "User is not subscribed to this notification type";
    public static final String NOTIFICATION_TYPE_IS_NOT_SUPPORTED = "Notification type is not supported";
    public static final String UNEXPECTED_INTERNAL_SERVER_ERROR = "Unexpected internal server error";

    public static final String USER_WITH_ID = "User with id ";
    public static final String NOT_FOUND = " not found";
    public static final String IS_NOT_SUPPORTED = "' is not supported";
}