package de.dkb.api.notificationhub.service.strategy;

import de.dkb.api.notificationhub.domain.Category;
import de.dkb.api.notificationhub.domain.NotificationType;
import de.dkb.api.notificationhub.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CategoryBNotificationStrategy implements NotificationStrategy {

    private static final Logger log =
            LoggerFactory.getLogger(CategoryBNotificationStrategy.class);

    private static final String STRATEGY_NAME = "CategoryB";

    @Override
    public boolean supports(NotificationType notificationType) {
        return Category.CATEGORY_B.getTypes().contains(notificationType);
    }

    @Override
    public void send(User user, NotificationType type, String message) {

        log.info("operation=NotificationDelivery strategy={} status=Started userId={} notificationType={}",
                STRATEGY_NAME,
                user.getId(),
                type);

        // we can integrate it here
        // we should not log the message in PROD, here
        try {

            // TODO: Call the Third party vendor

            log.info("operation=NotificationDelivery strategy={} status=Success userId={} notificationType={}",
                    STRATEGY_NAME,
                    user.getId(),
                    type);

        } catch (Exception ex) {

            log.error("operation=NotificationDelivery strategy={} status=Failed userId={} notificationType={} error={}",
                    STRATEGY_NAME,
                    user.getId(),
                    type,
                    ex.getMessage(),
                    ex);

            throw ex;
        }
    }
}