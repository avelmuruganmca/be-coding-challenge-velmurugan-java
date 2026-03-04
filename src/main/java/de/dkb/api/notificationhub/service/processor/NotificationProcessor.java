package de.dkb.api.notificationhub.service.processor;

import de.dkb.api.notificationhub.service.factory.NotificationStrategyFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationProcessor extends AbstractNotificationProcessor {

    public NotificationProcessor(NotificationStrategyFactory factory) {
        super(factory);
    }
}
