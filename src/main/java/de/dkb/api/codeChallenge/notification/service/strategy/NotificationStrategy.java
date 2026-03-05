package de.dkb.api.codeChallenge.notification.service.strategy;

import de.dkb.api.codeChallenge.notification.domain.NotificationType;
import de.dkb.api.codeChallenge.notification.entity.User;

public interface NotificationStrategy {

    boolean supports(NotificationType type);

    void send(User user, NotificationType type, String message);
}
