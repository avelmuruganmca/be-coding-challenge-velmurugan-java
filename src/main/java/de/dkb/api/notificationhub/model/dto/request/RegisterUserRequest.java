package de.dkb.api.notificationhub.model.dto.request;

import de.dkb.api.notificationhub.model.validation.ValidNotificationTypes;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

/**
 * API request DTO for user registration with notification preferences.
 * Separate from entity layer to maintain OpenAPI contract independence.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @NotNull(message = "UserId must not be null")
    private UUID userId;

    @ValidNotificationTypes  // add this
    @NotEmpty(message = "Notifications list must not be empty")
    @Size(max = 20, message = "Maximum 20 notification types allowed")
    private List<@NotEmpty(message = "Notification type cannot be empty") String> notifications;
}