package de.dkb.api.codeChallenge.notification.controller.dto.response;

import de.dkb.api.codeChallenge.notification.domain.NotificationType;

import java.util.UUID;

public record NotificationResponse(
        UUID userId,
        NotificationType notificationType,
        String status
) {}