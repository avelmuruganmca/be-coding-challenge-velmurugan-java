package de.dkb.api.codeChallenge.notification.controller.dto.response;

import de.dkb.api.codeChallenge.notification.domain.NotificationType;

import java.util.Set;
import java.util.UUID;

public record RegisterUserResponse(
        UUID userId,
        Set<NotificationType> registeredTypes
) {}
