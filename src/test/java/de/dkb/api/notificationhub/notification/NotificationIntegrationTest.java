package de.dkb.api.notificationhub.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dkb.api.notificationhub.notification.domain.NotificationType;
import de.dkb.api.notificationhub.notification.model.dto.request.NotificationDto;
import de.dkb.api.notificationhub.notification.model.dto.request.RegisterUserRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the notification API.
 * Follows AAA pattern and global standards for REST API testing.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Notification API Integration Tests")
class NotificationIntegrationTest {

    private static final String BASE_PATH = "/api/v1";
    private static final String REGISTER_PATH = BASE_PATH + "/subscriptions";
    private static final String NOTIFY_PATH = BASE_PATH + "/notifications";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // REGISTER ENDPOINT
    // =========================================================

    @Nested
    @DisplayName("POST /register - User Registration")
    class RegisterEndpointTests {

        @Test
        @DisplayName("should return 201 when user registers with valid preferences")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Arrange
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .userId(UUID.randomUUID())
                    .notifications(List.of("type1", "type2"))
                    .build();

            // Act
            ResultActions result = mockMvc.perform(post(REGISTER_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // Assert
            result
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("User registered successfully"))
                    .andExpect(jsonPath("$.traceId").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.errorCode").doesNotExist());
        }

        @Test
        @DisplayName("should return 400 when notification type is invalid")
        void shouldReturn400ForInvalidNotificationType() throws Exception {
            // Arrange
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .userId(UUID.randomUUID())
                    .notifications(List.of("INVALID_TYPE"))
                    .build();

            // Act & Assert
            mockMvc.perform(post(REGISTER_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VAL-001"))
                    .andExpect(jsonPath("$.traceId").exists());
        }

        @Test
        @DisplayName("should return 400 when notification type does not exist (type7)")
        void shouldReturn400ForUnsupportedNotificationType() throws Exception {
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .userId(UUID.randomUUID())
                    .notifications(List.of("type7"))
                    .build();

            mockMvc.perform(post(REGISTER_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VAL-001"));
        }

        @Test
        @DisplayName("should return 400 when request body is invalid")
        void shouldReturn400ForInvalidRegisterBody() throws Exception {
            mockMvc.perform(post(REGISTER_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").exists());
        }
    }

    // =========================================================
    // NOTIFY ENDPOINT - Success
    // =========================================================

    @Nested
    @DisplayName("POST /notify - Send Notification (Success)")
    class NotifySuccessTests {

        @Test
        @DisplayName("should return 200 when user is registered and subscribed")
        void shouldSendNotificationSuccessfully() throws Exception {
            // Arrange
            UUID userId = UUID.randomUUID();
            registerUser(userId, List.of("type1"));

            NotificationDto dto = new NotificationDto();
            dto.setUserId(userId);
            dto.setNotificationType(NotificationType.type1);
            dto.setMessage("Test message");

            // Act
            ResultActions result = mockMvc.perform(post(NOTIFY_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)));

            // Assert
            result
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Notification processed successfully"))
                    .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.data.notificationType").value("type1"))
                    .andExpect(jsonPath("$.traceId").exists())
                    .andExpect(jsonPath("$.errorCode").doesNotExist());
        }

        @Test
        @DisplayName("should deliver notification when user subscribed to same category (type1 -> type2)")
        void shouldDeliverWhenSubscribedToCategory() throws Exception {
            UUID userId = UUID.randomUUID();
            registerUser(userId, List.of("type1"));  // Category A

            NotificationDto dto = new NotificationDto();
            dto.setUserId(userId);
            dto.setNotificationType(NotificationType.type2);  // Also Category A
            dto.setMessage("Category test");

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Notification processed successfully"));
        }
    }

    // =========================================================
    // NOTIFY ENDPOINT - Error Cases
    // =========================================================

    @Nested
    @DisplayName("POST /notify - Error Responses")
    class NotifyErrorTests {

        @Test
        @DisplayName("should return 404 when user does not exist")
        void shouldReturn404WhenUserNotFound() throws Exception {
            NotificationDto dto = new NotificationDto();
            dto.setUserId(UUID.randomUUID());
            dto.setNotificationType(NotificationType.type1);
            dto.setMessage("Test");

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("USR-404"))
                    .andExpect(jsonPath("$.traceId").exists());
        }

        @Test
        @DisplayName("should return 403 when user is not subscribed to notification type")
        void shouldReturn403WhenUserNotSubscribed() throws Exception {
            UUID userId = UUID.randomUUID();
            registerUser(userId, List.of("type1"));  // Category A only

            NotificationDto dto = new NotificationDto();
            dto.setUserId(userId);
            dto.setNotificationType(NotificationType.type4);  // Category B
            dto.setMessage("Test");

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("USR-403"));
        }

        @Test
        @DisplayName("should return 400 when notification type is invalid (not in enum)")
        void shouldReturn400ForUnsupportedType() throws Exception {
            UUID userId = UUID.randomUUID();
            registerUser(userId, List.of("type1"));

            String json = """
            {"userId":"%s","notificationType":"type7","message":"test"}
            """.formatted(userId);

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("NOT-400"));
        }

        @Test
        @DisplayName("should return 400 when request validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            NotificationDto dto = new NotificationDto();
            dto.setUserId(null);
            dto.setNotificationType(null);
            dto.setMessage("");

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("VAL-001"))
                    .andExpect(jsonPath("$.data").isMap());
        }

        @Test
        @DisplayName("should return 400 when enum value is invalid")
        void shouldReturn400ForInvalidEnum() throws Exception {
            String invalidJson = """
                    {"userId":"11111111-1111-1111-1111-111111111111","notificationType":"INVALID","message":"test"}
                    """;

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("NOT-400"));
        }
    }

    // =========================================================
    // API DOCUMENTATION
    // =========================================================

    @Nested
    @DisplayName("API Documentation")
    class ApiDocumentationTests {

        @Test
        @DisplayName("should expose OpenAPI spec at /v3/api-docs")
        void shouldExposeOpenApiSpec() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.openapi").exists())
                    .andExpect(jsonPath("$.paths").exists())
                    .andExpect(jsonPath("$.paths./api/v1/subscriptions").exists())
                    .andExpect(jsonPath("$.paths./api/v1/notifications").exists());
        }

        @Test
        @DisplayName("should expose Swagger UI")
        void shouldExposeSwaggerUi() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================
    // OBSERVABILITY
    // =========================================================

    @Nested
    @DisplayName("Observability")
    class ObservabilityTests {

        @Test
        @DisplayName("should log notification delivery")
        void shouldLogNotificationDelivery() throws Exception {
            UUID userId = UUID.randomUUID();
            registerUser(userId, List.of("type1"));

            NotificationDto dto = new NotificationDto();
            dto.setUserId(userId);
            dto.setNotificationType(NotificationType.type1);
            dto.setMessage("Log test");

            mockMvc.perform(post(NOTIFY_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Notification processed successfully"));
        }

        @Test
        @DisplayName("should return X-Trace-Id header in response")
        void shouldReturnTraceIdHeader() throws Exception {
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .userId(UUID.randomUUID())
                    .notifications(List.of("type1"))
                    .build();

            mockMvc.perform(post(REGISTER_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(header().exists("X-Trace-Id"));
        }
    }

    // =========================================================
    // CONCURRENCY
    // =========================================================

    @Nested
    @DisplayName("Concurrency")
    class ConcurrencyTests {

        @Test
        @DisplayName("should handle concurrent notification requests")
        void shouldHandleConcurrentNotifications() throws Exception {
            UUID userId = UUID.randomUUID();
            registerUser(userId, List.of("type1"));

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            Callable<Integer> task = () -> {
                NotificationDto dto = new NotificationDto();
                dto.setUserId(userId);
                dto.setNotificationType(NotificationType.type1);
                dto.setMessage("Concurrent");

                return mockMvc.perform(post(NOTIFY_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                        .andReturn()
                        .getResponse()
                        .getStatus();
            };

            var futures = executor.invokeAll(
                    Stream.generate(() -> task).limit(threadCount).toList()
            );

            for (Future<Integer> future : futures) {
                assertThat(future.get()).isEqualTo(200);
            }

            executor.shutdown();
        }
    }

    // =========================================================
    // HELPERS
    // =========================================================

    private void registerUser(UUID userId, List<String> notifications) throws Exception {
        RegisterUserRequest request = RegisterUserRequest.builder()
                .userId(userId)
                .notifications(notifications)
                .build();
        mockMvc.perform(post(REGISTER_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }
}