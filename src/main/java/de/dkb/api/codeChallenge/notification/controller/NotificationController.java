package de.dkb.api.codeChallenge.notification.controller;

import de.dkb.api.codeChallenge.notification.config.ApplicationMetadata;
import de.dkb.api.codeChallenge.notification.entity.User;
import de.dkb.api.codeChallenge.notification.model.GenericResponse;
import de.dkb.api.codeChallenge.notification.model.NotificationDto;
import de.dkb.api.codeChallenge.notification.model.RegisterUserRequest;
import de.dkb.api.codeChallenge.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService service;
    private final ApplicationMetadata metadata;

    public NotificationController(NotificationService service,
                                  ApplicationMetadata metadata) {
        this.service = service;
        this.metadata = metadata;
    }

    // =========================
    // REGISTER USER
    // =========================
    @Operation(summary = "Register user with notification preferences")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/register")
    public ResponseEntity<GenericResponse<Void>> register(
            @Valid @RequestBody RegisterUserRequest request,
            HttpServletRequest httpRequest) {

        log.info("operation=RegisterUser status=Started userId={}",
                request.getUserId());

        User user = service.registerUser(request);

        log.info("operation=RegisterUser status=Completed userId={}",
                user.getId());

        String traceId = MDC.get("traceId");

        return ResponseEntity.status(201)
                .body(GenericResponse.success(
                        metadata.getServiceName(),
                        traceId,
                        httpRequest.getRequestURI(),
                        201,
                        "User registered successfully",
                        null
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
    @PostMapping("/notify")
    public ResponseEntity<GenericResponse<Map<String, Object>>> notify(
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

        Map<String, Object> payload = Map.of(
                "userId", dto.getUserId(),
                "notificationType", dto.getNotificationType()
        );

        return ResponseEntity.ok(
                GenericResponse.success(
                        metadata.getServiceName(),
                        traceId,
                        httpRequest.getRequestURI(),
                        200,
                        "Notification processed successfully",
                        payload
                )
        );
    }
}