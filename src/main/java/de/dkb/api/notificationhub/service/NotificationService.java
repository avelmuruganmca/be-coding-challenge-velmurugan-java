package de.dkb.api.notificationhub.service;

import de.dkb.api.notificationhub.entity.User;
import de.dkb.api.notificationhub.exception.UserNotFoundException;
import de.dkb.api.notificationhub.mapper.UserMapper;
import de.dkb.api.notificationhub.model.dto.request.NotificationRequest;
import de.dkb.api.notificationhub.model.dto.request.RegisterUserRequest;
import de.dkb.api.notificationhub.model.dto.response.RegisterUserResponse;
import de.dkb.api.notificationhub.repository.UserRepository;
import de.dkb.api.notificationhub.service.processor.NotificationProcessor;
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
    private final UserMapper userMapper;
    private final MeterRegistry meterRegistry;

    private final Counter notificationSuccessCounter;

    public NotificationService(UserRepository userRepository,
                               NotificationProcessor processor,
                               UserMapper userMapper,
                               MeterRegistry meterRegistry) {

        this.userRepository = userRepository;
        this.processor = processor;
        this.userMapper = userMapper;
        this.meterRegistry = meterRegistry;

        // Register once (global success counter)
        this.notificationSuccessCounter = Counter.builder("notification.success.total")
                .description("Total number of successfully processed notifications")
                .register(meterRegistry);
    }

    // =========================================================
    // REGISTER USER
    // =========================================================
    public RegisterUserResponse registerUser(RegisterUserRequest request) {

        log.info("operation={} status=Started userId={}",
                OP_REGISTER,
                request.getUserId());

        String notifications = String.join(";", request.getNotifications());

        User user = new User(request.getUserId(), notifications);

        User savedUser = userRepository.save(user);

        log.info("operation={} status=Completed userId={}",
                OP_REGISTER,
                savedUser.getId());

        return userMapper.toRegisterUserResponse(savedUser);
    }

    // =========================================================
    // SEND NOTIFICATION
    // =========================================================
    @Timed(
            value = "notification.processing",
            histogram = true
    )
    public void sendNotification(NotificationRequest request) {

        log.info("operation={} status=Started userId={} notificationType={}",
                OP_NOTIFY,
                request.getUserId(),
                request.getNotificationType());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> {
                    log.warn("operation={} status=Failed reason=UserNotFound userId={}",
                            OP_NOTIFY,
                            request.getUserId());
                    return new UserNotFoundException(request.getUserId().toString());
                });

        processor.process(user, request.getNotificationType(), request.getMessage());

        // ✅ Global success counter
        notificationSuccessCounter.increment();

        // ✅ Per-notification-type counter
        incrementNotificationByType(request.getNotificationType().name());

        log.info("operation={} status=Completed userId={} notificationType={}",
                OP_NOTIFY,
                request.getUserId(),
                request.getNotificationType());
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