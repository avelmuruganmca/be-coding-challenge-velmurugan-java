package de.dkb.api.notificationhub.notification.service.processor;

import de.dkb.api.notificationhub.notification.domain.Category;
import de.dkb.api.notificationhub.notification.domain.NotificationType;
import de.dkb.api.notificationhub.notification.entity.User;
import de.dkb.api.notificationhub.notification.exception.UnsupportedNotificationTypeException;
import de.dkb.api.notificationhub.notification.exception.UserNotSubscribedException;
import de.dkb.api.notificationhub.notification.service.factory.NotificationStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNotificationProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(AbstractNotificationProcessor.class);

    protected final NotificationStrategyFactory factory;

    protected AbstractNotificationProcessor(NotificationStrategyFactory factory) {
        this.factory = factory;
    }

    public final void process(User user, NotificationType type, String message) {

        log.debug("operation=ProcessNotification stage=Start userId={} notificationType={}",
                user.getId(),
                type);

        validate(type);
        checkSubscription(user, type);
        doSend(user, type, message);

        log.debug("operation=ProcessNotification stage=Completed userId={} notificationType={}",
                user.getId(),
                type);
    }

    protected void validate(NotificationType type) {
        if (type == null) {
            log.warn("operation=ProcessNotification status=Failed reason=NullNotificationType");
            throw new IllegalArgumentException("Notification type cannot be null");
        }
        if (!Category.isSupported(type)) {
            log.warn("operation=ProcessNotification status=Failed reason=UnsupportedNotificationType type={}", type);
            throw new UnsupportedNotificationTypeException(type);
        }
    }

    protected void checkSubscription(User user, NotificationType type) {

        var category =
                Category.from(type);

        boolean subscribed = category.getTypes()
                .stream()
                .anyMatch(user.getNotificationTypeSet()::contains);

        if (!subscribed) {

            log.warn("operation=ProcessNotification status=Failed reason=UserNotSubscribed userId={} notificationType={}",
                    user.getId(),
                    type);

            throw new UserNotSubscribedException();
        }

        log.debug("operation=ProcessNotification stage=SubscriptionValidated userId={} notificationType={}",
                user.getId(),
                type);
    }

    protected void doSend(User user, NotificationType type, String message) {

        log.debug("operation=ProcessNotification stage=StrategyResolved userId={} notificationType={}",
                user.getId(),
                type);

        factory.getStrategy(type).send(user, type, message);

        log.info("operation=NotificationSent userId={} notificationType={}",
                user.getId(),
                type);
    }
}