package de.dkb.api.notificationhub.notification.service;

import de.dkb.api.notificationhub.notification.entity.User;
import de.dkb.api.notificationhub.notification.exception.UserNotFoundException;
import de.dkb.api.notificationhub.notification.model.dto.request.NotificationDto;
import de.dkb.api.notificationhub.notification.model.dto.request.RegisterUserRequest;
import de.dkb.api.notificationhub.notification.repository.UserRepository;
import de.dkb.api.notificationhub.notification.service.processor.NotificationProcessor;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationService.class);

    private static final String OP_REGISTER = "RegisterUser";
    private static final String OP_NOTIFY = "SendNotification";

    private final UserRepository userRepository;
    private final NotificationProcessor processor;
    private final MeterRegistry meterRegistry;

    private final Counter notificationSuccessCounter;

    public NotificationService(UserRepository userRepository,
                               NotificationProcessor processor,
                               MeterRegistry meterRegistry) {

        this.userRepository = userRepository;
        this.processor = processor;
        this.meterRegistry = meterRegistry;

        // Register once (global success counter)
        this.notificationSuccessCounter = Counter.builder("notification.success.total")
                .description("Total number of successfully processed notifications")
                .register(meterRegistry);
    }

    // =========================================================
    // REGISTER USER
    // =========================================================
    public User registerUser(RegisterUserRequest request) {

        log.info("operation={} status=Started userId={}",
                OP_REGISTER,
                request.getUserId());

        String notifications = String.join(";", request.getNotifications());

        User user = new User(request.getUserId(), notifications);

        User savedUser = userRepository.save(user);

        log.info("operation={} status=Completed userId={}",
                OP_REGISTER,
                savedUser.getId());

        return savedUser;
    }

    // =========================================================
    // SEND NOTIFICATION
    // =========================================================
    @Timed(
            value = "notification.processing",
            histogram = true
    )
    public void sendNotification(NotificationDto dto) {

        log.info("operation={} status=Started userId={} notificationType={}",
                OP_NOTIFY,
                dto.getUserId(),
                dto.getNotificationType());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    log.warn("operation={} status=Failed reason=UserNotFound userId={}",
                            OP_NOTIFY,
                            dto.getUserId());
                    return new UserNotFoundException(dto.getUserId().toString());
                });

        processor.process(user, dto.getNotificationType(), dto.getMessage());

        // ✅ Global success counter
        notificationSuccessCounter.increment();

        // ✅ Per-notification-type counter
        incrementNotificationByType(dto.getNotificationType().name());

        log.info("operation={} status=Completed userId={} notificationType={}",
                OP_NOTIFY,
                dto.getUserId(),
                dto.getNotificationType());
    }

    // =========================================================
    // Per-Type Metrics
    // =========================================================
    private void incrementNotificationByType(String type) {
        Counter.builder("notification.processed.total")
                .description("Notifications processed grouped by notification type")
                .tag("type", type)
                .register(meterRegistry)
                .increment();
    }
}