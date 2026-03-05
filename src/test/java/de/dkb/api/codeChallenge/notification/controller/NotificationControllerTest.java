package de.dkb.api.codeChallenge.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dkb.api.codeChallenge.notification.controller.dto.request.NotificationRequest;
import de.dkb.api.codeChallenge.notification.controller.dto.request.RegisterUserRequest;
import de.dkb.api.codeChallenge.notification.controller.dto.response.RegisterUserResponse;
import de.dkb.api.codeChallenge.notification.domain.NotificationType;
import de.dkb.api.codeChallenge.notification.service.NotificationService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("NotificationController MockMvc Tests")
class NotificationControllerTest {

    private static final String REGISTER_PATH = "/api/v1/register";
    private static final String NOTIFY_PATH = "/api/v1/notify";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private MeterRegistry meterRegistry;

    @Nested
    @DisplayName("POST /register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("should return 201 and delegate to service")
        void shouldReturn201OnRegister() throws Exception {
            UUID userId = UUID.randomUUID();
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .userId(userId)
                    .notifications(List.of("type1", "type2"))
                    .build();
            RegisterUserResponse response = new RegisterUserResponse(userId, Set.of(NotificationType.type1, NotificationType.type2));
            when(notificationService.registerUser(any(RegisterUserRequest.class))).thenReturn(response);

            mockMvc.perform(post(REGISTER_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("User registered successfully"))
                    .andExpect(jsonPath("$.traceId").exists())
                    .andExpect(jsonPath("$.data").exists());

            verify(notificationService).registerUser(argThat(req ->
                    req.getUserId().equals(userId) && req.getNotifications().equals(List.of("type1", "type2"))
            ));
        }

        @Test
        @DisplayName("should return 400 when request validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            mockMvc.perform(post(REGISTER_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /notify")
    class NotifyEndpointTests {

        @Test
        @DisplayName("should return 200 and delegate to service")
        void shouldReturn200OnNotify() throws Exception {
            UUID userId = UUID.randomUUID();
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setNotificationType(NotificationType.type1);
            request.setMessage("Test message");

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Notification processed successfully"))
                    .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.data.notificationType").value("type1"))
                    .andExpect(jsonPath("$.data.status").value("SENT"))
                    .andExpect(jsonPath("$.traceId").exists());

            verify(notificationService).sendNotification(argThat(req ->
                    req.getUserId().equals(userId) && req.getNotificationType() == NotificationType.type1 && "Test message".equals(req.getMessage())
            ));
        }

        @Test
        @DisplayName("should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(null);
            request.setNotificationType(null);
            request.setMessage("");

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
