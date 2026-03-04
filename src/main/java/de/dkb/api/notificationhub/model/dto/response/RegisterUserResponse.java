package de.dkb.api.notificationhub.model.dto.response;

import de.dkb.api.notificationhub.domain.NotificationType;

import java.util.Set;
import java.util.UUID;

/**
 * API response DTO for user registration.
 * Separate from entity layer for OpenAPI contract independence.
 */
public record RegisterUserResponse(
        UUID userId,
        Set<NotificationType> registeredTypes
) {}
