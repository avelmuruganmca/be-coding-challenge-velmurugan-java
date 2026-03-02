package de.dkb.api.codeChallenge.notification.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @NotNull(message = "UserId must not be null")
    private UUID userId;

    @NotEmpty(message = "Notifications list must not be empty")
    @Size(max = 20, message = "Maximum 20 notification types allowed")
    private List<@NotEmpty(message = "Notification type cannot be empty") String> notifications;
}