package de.dkb.api.codeChallenge.notification.service;

import de.dkb.api.codeChallenge.notification.entity.User;
import de.dkb.api.codeChallenge.notification.exception.UserNotFoundException;
import de.dkb.api.codeChallenge.notification.model.NotificationDto;
import de.dkb.api.codeChallenge.notification.model.RegisterUserRequest;
import de.dkb.api.codeChallenge.notification.repository.UserRepository;
import de.dkb.api.codeChallenge.notification.service.processor.NotificationProcessor;
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

    public NotificationService(UserRepository userRepository,
                               NotificationProcessor processor) {
        this.userRepository = userRepository;
        this.processor = processor;
    }

    // =========================
    // REGISTER USER
    // =========================
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

    // =========================
    // SEND NOTIFICATION
    // =========================
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

        log.info("operation={} status=Completed userId={} notificationType={}",
                OP_NOTIFY,
                dto.getUserId(),
                dto.getNotificationType());
    }
}