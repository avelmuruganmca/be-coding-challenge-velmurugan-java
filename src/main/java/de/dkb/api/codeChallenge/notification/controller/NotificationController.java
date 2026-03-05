package de.dkb.api.codeChallenge.notification.controller;

import de.dkb.api.codeChallenge.notification.common.ApiMessages;
import de.dkb.api.codeChallenge.notification.common.GenericResponse;
import de.dkb.api.codeChallenge.notification.controller.dto.request.NotificationRequest;
import de.dkb.api.codeChallenge.notification.controller.dto.request.RegisterUserRequest;
import de.dkb.api.codeChallenge.notification.controller.dto.response.NotificationResponse;
import de.dkb.api.codeChallenge.notification.controller.dto.response.RegisterUserResponse;
import de.dkb.api.codeChallenge.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<GenericResponse<RegisterUserResponse>> register(
            @Valid @RequestBody RegisterUserRequest request,
            HttpServletRequest httpRequest) {

        log.info("operation=RegisterUser status=Started userId={}",
                request.getUserId());

        RegisterUserResponse response = service.registerUser(request);

        log.info("operation=RegisterUser status=Completed userId={}",
                response.userId());

        String traceId = MDC.get("traceId");

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
            @ApiResponse(responseCode = "200", description = "Notification processed successfully",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid notification type",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))),
            @ApiResponse(responseCode = "403", description = "User not subscribed",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = GenericResponse.class)))
    })
    @PostMapping("/notify")
    public ResponseEntity<GenericResponse<NotificationResponse>> notify(
            @Valid @RequestBody NotificationRequest request,
            HttpServletRequest httpRequest) {

        log.info("operation=SendNotification status=Started userId={} notificationType={}",
                request.getUserId(),
                request.getNotificationType());

        service.sendNotification(request);

        log.info("operation=SendNotification status=Completed userId={} notificationType={}",
                request.getUserId(),
                request.getNotificationType());

        String traceId = MDC.get("traceId");

        NotificationResponse response =
                new NotificationResponse(
                        request.getUserId(),
                        request.getNotificationType(),
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