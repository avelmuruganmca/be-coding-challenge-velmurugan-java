package de.dkb.api.notificationhub.notification.model.dto.response;

import de.dkb.api.notificationhub.notification.domain.NotificationType;

import java.util.Set;
import java.util.UUID;

public record RegisterUserResponse(
        UUID userId,
        Set<NotificationType> registeredTypes
) {}
