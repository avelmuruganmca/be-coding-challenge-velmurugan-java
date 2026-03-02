package de.dkb.api.codeChallenge.notification.service.processor;

import de.dkb.api.codeChallenge.notification.entity.User;
import de.dkb.api.codeChallenge.notification.exception.UserNotSubscribedException;
import de.dkb.api.codeChallenge.notification.model.NotificationType;
import de.dkb.api.codeChallenge.notification.service.factory.NotificationStrategyFactory;
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
    }

    protected void checkSubscription(User user, NotificationType type) {

        var category =
                de.dkb.api.codeChallenge.notification.model.Category.from(type);

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