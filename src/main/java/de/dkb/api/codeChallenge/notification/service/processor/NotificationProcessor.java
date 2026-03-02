package de.dkb.api.codeChallenge.notification.service.processor;

import de.dkb.api.codeChallenge.notification.service.factory.NotificationStrategyFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationProcessor extends AbstractNotificationProcessor {

    public NotificationProcessor(NotificationStrategyFactory factory) {
        super(factory);
    }
}
