package de.dkb.api.codeChallenge.notification.service.factory;

import de.dkb.api.codeChallenge.notification.domain.NotificationType;
import de.dkb.api.codeChallenge.notification.service.strategy.NotificationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationStrategyFactory {

    public static final String NO_STRATEGY_FOUND_FOR_TYPE = "No strategy found for type: ";
    private final List<NotificationStrategy> strategies;

    public NotificationStrategyFactory(List<NotificationStrategy> strategies) {
        this.strategies = strategies;
    }

    public NotificationStrategy getStrategy(NotificationType notificationType) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(notificationType))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(NO_STRATEGY_FOUND_FOR_TYPE + notificationType));
    }
}
