package de.dkb.api.notificationhub.model.dto.request;

import de.dkb.api.notificationhub.domain.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * API request DTO for sending a notification.
 * Separate from entity layer to maintain OpenAPI contract independence.
 */
@Getter
@Setter
public class NotificationRequest {

    @NotNull(message = "UserId must not be null")
    private UUID userId;

    @NotNull(message = "NotificationType must not be null")
    private NotificationType notificationType;

    @NotBlank(message = "Message must not be blank")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
}
