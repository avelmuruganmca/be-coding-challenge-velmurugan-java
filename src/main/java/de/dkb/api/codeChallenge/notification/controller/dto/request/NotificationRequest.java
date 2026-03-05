package de.dkb.api.codeChallenge.notification.controller.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.dkb.api.codeChallenge.notification.controller.dto.serializer.UuidDeserializer;
import de.dkb.api.codeChallenge.notification.domain.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NotificationRequest {

    @NotNull(message = "UserId must not be null")
    @JsonDeserialize(using = UuidDeserializer.class)
    private UUID userId;

    @NotNull(message = "NotificationType must not be null")
    private NotificationType notificationType;

    @NotBlank(message = "Message must not be blank")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;
}
