package de.dkb.api.notificationhub.notification.controller;

import de.dkb.api.notificationhub.notification.common.ApiMessages;
import de.dkb.api.notificationhub.notification.common.GenericResponse;
import de.dkb.api.notificationhub.notification.entity.User;
import de.dkb.api.notificationhub.notification.model.dto.request.NotificationDto;
import de.dkb.api.notificationhub.notification.model.dto.request.RegisterUserRequest;
import de.dkb.api.notificationhub.notification.model.dto.response.NotificationResponse;
import de.dkb.api.notificationhub.notification.model.dto.response.RegisterUserResponse;
import de.dkb.api.notificationhub.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationController.class);

    @Autowired
    private NotificationService service;




    // =========================
    // REGISTER USER
    // =========================
    @Operation(summary = "Register user with notification preferences")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/subscriptions")
    public ResponseEntity<GenericResponse<RegisterUserResponse>> register(
            @Valid @RequestBody RegisterUserRequest request,
            HttpServletRequest httpRequest) {

        log.info("operation=RegisterUser status=Started userId={}",
                request.getUserId());

        User user = service.registerUser(request);

        log.info("operation=RegisterUser status=Completed userId={}",
                user.getId());

        String traceId = MDC.get("traceId");

        RegisterUserResponse response =
                new RegisterUserResponse(
                        user.getId(),
                        user.getNotificationTypeSet()
                );

        return ResponseEntity.status(201)
                .body(GenericResponse.success(
                        traceId,
                        ApiMessages.USER_REGISTERED_SUCCESS,
                        response
                ));
    }

    // =========================
    // SEND NOTIFICATION
    // =========================
    @Operation(summary = "Send notification to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid notification type"),
            @ApiResponse(responseCode = "403", description = "User not subscribed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/notifications")
    public ResponseEntity<GenericResponse<NotificationResponse>> notify(
            @Valid @RequestBody NotificationDto dto,
            HttpServletRequest httpRequest) {

        log.info("operation=SendNotification status=Started userId={} notificationType={}",
                dto.getUserId(),
                dto.getNotificationType());

        service.sendNotification(dto);

        log.info("operation=SendNotification status=Completed userId={} notificationType={}",
                dto.getUserId(),
                dto.getNotificationType());

        String traceId = MDC.get("traceId");

        NotificationResponse response =
                new NotificationResponse(
                        dto.getUserId(),
                        dto.getNotificationType(),
                        ApiMessages.NOTIFICATION_STATUS_SENT
                );

        return ResponseEntity.ok(
                GenericResponse.success(
                        traceId,
                        ApiMessages.NOTIFICATION_PROCESSED_SUCCESS,
                        response
                )
        );
    }
}