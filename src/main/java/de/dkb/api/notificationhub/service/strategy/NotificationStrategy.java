package de.dkb.api.notificationhub.service.strategy;

import de.dkb.api.notificationhub.domain.NotificationType;
import de.dkb.api.notificationhub.entity.User;

public interface NotificationStrategy {

    boolean supports(NotificationType type);

    void send(User user, NotificationType type, String message);
}
