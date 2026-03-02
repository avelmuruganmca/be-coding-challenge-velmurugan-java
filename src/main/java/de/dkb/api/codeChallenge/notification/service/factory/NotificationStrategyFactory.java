package de.dkb.api.codeChallenge.notification.service.factory;

import de.dkb.api.codeChallenge.notification.model.NotificationType;
import de.dkb.api.codeChallenge.notification.service.strategy.NotificationStrategy;
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
