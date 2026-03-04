package de.dkb.api.notificationhub.model.dto.response;

import de.dkb.api.notificationhub.domain.NotificationType;

import java.util.UUID;

/**
 * API response DTO for notification delivery.
 * Separate from entity layer for OpenAPI contract independence.
 */
public record NotificationResponse(
        UUID userId,
        NotificationType notificationType,
        String status
) {}