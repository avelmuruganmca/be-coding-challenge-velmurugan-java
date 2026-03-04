package de.dkb.api.notificationhub.notification.service.factory;

import de.dkb.api.notificationhub.notification.domain.NotificationType;
import de.dkb.api.notificationhub.notification.service.strategy.NotificationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationStrategyFactory {

    private final List<NotificationStrategy> strategies;

    public NotificationStrategyFactory(List<NotificationStrategy> strategies) {
        this.strategies = strategies;
    }

    public NotificationStrategy getStrategy(NotificationType notificationType) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(notificationType))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("No strategy found for type: " + notificationType));
    }
}
