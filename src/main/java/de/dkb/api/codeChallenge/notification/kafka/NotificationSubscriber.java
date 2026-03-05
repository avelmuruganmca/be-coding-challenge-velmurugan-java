package de.dkb.api.codeChallenge.notification.kafka;

/* Kafka placeholder - uncomment when Kafka integration is needed */

// import de.dkb.api.codeChallenge.notification.model.dto.request.NotificationRequest;
// import de.dkb.api.codeChallenge.notification.service.NotificationService;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Component;
//
// @Component
// public class NotificationSubscriber {
//
//     private final NotificationService notificationService;
//
//     public NotificationSubscriber(NotificationService notificationService) {
//         this.notificationService = notificationService;
//     }
//
//     @KafkaListener(
//         topics = {"notifications"},
//         groupId = "codechallenge_group",
//         autoStartup = "${kafka.listener.enabled:false}"
//     )
//     public void consumeNotification(NotificationRequest request) {
//         notificationService.sendNotification(request);
//     }
// }

