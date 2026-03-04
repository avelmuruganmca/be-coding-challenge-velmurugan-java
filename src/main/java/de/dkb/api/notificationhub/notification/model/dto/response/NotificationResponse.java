package de.dkb.api.notificationhub.notification.model.dto.response;

import de.dkb.api.notificationhub.notification.domain.NotificationType;

import java.util.UUID;

public record NotificationResponse(
        UUID userId,
        NotificationType notificationType,
        String status
) {}