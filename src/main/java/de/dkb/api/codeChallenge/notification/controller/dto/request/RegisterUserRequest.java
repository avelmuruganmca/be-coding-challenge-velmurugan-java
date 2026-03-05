package de.dkb.api.codeChallenge.notification.controller.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.dkb.api.codeChallenge.notification.controller.dto.serializer.UuidDeserializer;
import de.dkb.api.codeChallenge.notification.controller.dto.validation.ValidNotificationTypes;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @NotNull(message = "UserId must not be null")
    @JsonDeserialize(using = UuidDeserializer.class)
    private UUID userId;

    @ValidNotificationTypes
    @NotEmpty(message = "Notifications list must not be empty")
    @Size(max = 20, message = "Maximum 20 notification types allowed")
    private List<@NotEmpty(message = "Notification type cannot be empty") String> notifications;
}