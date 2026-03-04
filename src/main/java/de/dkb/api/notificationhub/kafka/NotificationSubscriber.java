
package de.dkb.api.notificationhub.kafka;

 /*Kafka subscriber – deactivated for the challenge, but consider this for production use
 Uncomment and implement when Kafka is required:*/

// import de.dkb.api.notificationhub.model.dto.request.NotificationRequest;
// import de.dkb.api.notificationhub.service.NotificationService;
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
//         groupId = "notification-service_group",
//         autoStartup = "${kafka.listener.enabled:false}"
//     )
//     public void consumeNotification(NotificationRequest request) {
//         notificationService.sendNotification(request);
//     }
// }

