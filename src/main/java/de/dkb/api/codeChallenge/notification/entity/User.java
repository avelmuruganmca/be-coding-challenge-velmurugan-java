package de.dkb.api.codeChallenge.notification.entity;

import de.dkb.api.codeChallenge.notification.domain.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Entity
@Table(name = "users")
public class User {

    public static final String SEMI_COLON = ";";
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Setter
    @Column(nullable = false)
    private String notifications; // stored as "type1;type2"

    public User() {}

    public User(UUID id, String notifications) {
        this.id = id;
        this.notifications = notifications;
    }

    public Set<NotificationType> getNotificationTypeSet() {

        if (notifications == null || notifications.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(notifications.split(SEMI_COLON))
                .map(NotificationType::valueOf)
                .collect(Collectors.toSet());
    }

}