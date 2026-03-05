package de.dkb.api.codeChallenge.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dkb.api.codeChallenge.notification.controller.dto.request.NotificationRequest;
import de.dkb.api.codeChallenge.notification.controller.dto.request.RegisterUserRequest;
import de.dkb.api.codeChallenge.notification.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Register → Notify Flow Integration Test")
class RegisterNotifyFlowIntegrationTest {

    private static final String REGISTER_PATH = "/api/v1/register";
    private static final String NOTIFY_PATH = "/api/v1/notify";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("should complete full flow: register user, then send notification successfully")
    void shouldCompleteRegisterThenNotifyFlow() throws Exception {
        UUID userId = UUID.randomUUID();

        registerUser(userId, List.of("type1", "type2"));

        NotificationRequest notifyRequest = new NotificationRequest();
        notifyRequest.setUserId(userId);
        notifyRequest.setNotificationType(NotificationType.type1);
        notifyRequest.setMessage("Hello from integration test");

        mockMvc.perform(post(NOTIFY_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification processed successfully"))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.notificationType").value("type1"))
                .andExpect(jsonPath("$.data.status").value("SENT"))
                .andExpect(jsonPath("$.errorCode").doesNotExist());
    }

    @Test
    @DisplayName("should deliver notification for category subscription (register type1, notify type2)")
    void shouldDeliverForCategorySubscription() throws Exception {
        UUID userId = UUID.randomUUID();
        registerUser(userId, List.of("type1"));

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setNotificationType(NotificationType.type2);
        request.setMessage("Category A notification");

        mockMvc.perform(post(NOTIFY_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification processed successfully"))
                .andExpect(jsonPath("$.data.notificationType").value("type2"));
    }

    @Test
    @DisplayName("should fail notify with 404 when user was never registered")
    void shouldFailNotifyWhenUserNotRegistered() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(UUID.randomUUID());
        request.setNotificationType(NotificationType.type1);
        request.setMessage("Test");

        mockMvc.perform(post(NOTIFY_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("USR-404"));
    }

    @Test
    @DisplayName("should fail notify with 403 when user not subscribed to notification type")
    void shouldFailNotifyWhenNotSubscribed() throws Exception {
        UUID userId = UUID.randomUUID();
        registerUser(userId, List.of("type1"));

        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setNotificationType(NotificationType.type4);
        request.setMessage("Category B - not subscribed");

        mockMvc.perform(post(NOTIFY_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("USR-403"));
    }

    private void registerUser(UUID userId, List<String> notifications) throws Exception {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .userId(userId)
                .notifications(notifications)
                .build();
        mockMvc.perform(post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
