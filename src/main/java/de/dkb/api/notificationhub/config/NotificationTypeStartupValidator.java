package de.dkb.api.notificationhub.config;

import de.dkb.api.notificationhub.domain.Category;
import de.dkb.api.notificationhub.domain.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class NotificationTypeStartupValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(NotificationTypeStartupValidator.class);

    @Override
    public void run(ApplicationArguments args) {
        List<NotificationType> uncategorized = new ArrayList<>();
        for (NotificationType type : NotificationType.values()) {
            try {
                Category.from(type);
            } catch (IllegalArgumentException e) {
                uncategorized.add(type);
            }
        }
        if (!uncategorized.isEmpty()) {
            String message = String.format(
                    "NotificationType(s) %s are not assigned to any Category. Update the Category enum.",
                    uncategorized
            );
            log.error(message);
            throw new IllegalStateException(message);
        }
        log.info("Notification type validation passed: all types are categorized");
    }
}