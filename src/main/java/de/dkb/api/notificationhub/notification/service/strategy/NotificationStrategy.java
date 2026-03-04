package de.dkb.api.notificationhub.notification.service.strategy;

import de.dkb.api.notificationhub.notification.domain.NotificationType;
import de.dkb.api.notificationhub.notification.entity.User;

public interface NotificationStrategy {

    boolean supports(NotificationType type);

    void send(User user, NotificationType type, String message);
}
