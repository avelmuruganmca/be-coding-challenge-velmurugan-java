package de.dkb.api.codeChallenge.notification.entity;

import de.dkb.api.codeChallenge.notification.model.NotificationType;
import jakarta.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String notifications; // stored as "type1;type2"

    public User() {}

    public User(UUID id, String notifications) {
        this.id = id;
        this.notifications = notifications;
    }

    public UUID getId() {
        return id;
    }

    public String getNotifications() {
        return notifications;
    }

    public void setNotifications(String notifications) {
        this.notifications = notifications;
    }

    public Set<NotificationType> getNotificationTypeSet() {

        if (notifications == null || notifications.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(notifications.split(";"))
                .map(NotificationType::valueOf)
                .collect(Collectors.toSet());
    }

}