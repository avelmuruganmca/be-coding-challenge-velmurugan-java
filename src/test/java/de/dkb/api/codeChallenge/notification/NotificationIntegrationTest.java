package de.dkb.api.codeChallenge.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.dkb.api.codeChallenge.notification.model.NotificationDto;
import de.dkb.api.codeChallenge.notification.model.NotificationType;
import de.dkb.api.codeChallenge.notification.model.RegisterUserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================
    // REGISTER SUCCESS
    // =========================
    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        RegisterUserRequest request = new RegisterUserRequest();
        request.setUserId(UUID.randomUUID());
        request.setNotifications(List.of("type1", "type2"));

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/register"))
                .andExpect(header().exists("X-Trace-Id"));
    }

    // =========================
    // NOTIFY SUCCESS
    // =========================
    @Test
    void shouldSendNotificationSuccessfully() throws Exception {

        UUID userId = UUID.randomUUID();

        // Register first
        RegisterUserRequest register = new RegisterUserRequest();
        register.setUserId(userId);
        register.setNotifications(List.of("type1"));

        mockMvc.perform(post("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        NotificationDto dto = new NotificationDto();
        dto.setUserId(userId);
        dto.setNotificationType(NotificationType.type1);
        dto.setMessage("Test Message");

        mockMvc.perform(post("/api/v1/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/notify"));
    }

    // =========================
    // USER NOT FOUND
    // =========================
    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {

        NotificationDto dto = new NotificationDto();
        dto.setUserId(UUID.randomUUID());
        dto.setNotificationType(NotificationType.type1);
        dto.setMessage("Test");

        mockMvc.perform(post("/api/v1/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("USR-404"))
                .andExpect(jsonPath("$.traceId").exists())
                .andExpect(jsonPath("$.path").value("/api/v1/notify"));
    }

    // =========================
    // USER NOT SUBSCRIBED
    // =========================
    @Test
    void shouldReturn403WhenUserNotSubscribed() throws Exception {

        UUID userId = UUID.randomUUID();

        RegisterUserRequest register = new RegisterUserRequest();
        register.setUserId(userId);
        register.setNotifications(List.of("type1"));

        mockMvc.perform(post("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        NotificationDto dto = new NotificationDto();
        dto.setUserId(userId);
        dto.setNotificationType(NotificationType.type4); // Category B
        dto.setMessage("Test");

        mockMvc.perform(post("/api/v1/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("USR-403"));
    }

    // =========================
    // VALIDATION ERROR
    // =========================
    @Test
    void shouldReturn400WhenValidationFails() throws Exception {

        NotificationDto dto = new NotificationDto();
        dto.setUserId(null); // invalid
        dto.setNotificationType(null);
        dto.setMessage("");

        mockMvc.perform(post("/api/v1/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VAL-001"))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.traceId").exists());
    }

    @ExtendWith(OutputCaptureExtension.class)
    @Test
    void shouldLogNotificationDelivery(CapturedOutput output) throws Exception {

        UUID userId = UUID.randomUUID();

        RegisterUserRequest register = new RegisterUserRequest();
        register.setUserId(userId);
        register.setNotifications(List.of("type1"));

        mockMvc.perform(post("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        NotificationDto dto = new NotificationDto();
        dto.setUserId(userId);
        dto.setNotificationType(NotificationType.type1);
        dto.setMessage("Log test");

        mockMvc.perform(post("/api/v1/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        assertThat(output.getOut())
                .contains("operation=NotificationDelivery");
    }

    @Test
    void shouldExposeOpenApiDocs() throws Exception {

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths").exists());
    }

    @Test
    void shouldReturn400ForInvalidEnumValue() throws Exception {

        String invalidJson = """
        {
          "userId": "11111111-1111-1111-1111-111111111111",
          "notificationType": "INVALID_TYPE",
          "message": "test"
        }
        """;

        mockMvc.perform(post("/api/v1/notify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.traceId").exists());
    }


    @Test
    void shouldHandleConcurrentNotifications() throws Exception {

        UUID userId = UUID.randomUUID();

        RegisterUserRequest register = new RegisterUserRequest();
        register.setUserId(userId);
        register.setNotifications(List.of("type1"));

        mockMvc.perform(post("/api/v1/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        Callable<Integer> task = () -> {
            NotificationDto dto = new NotificationDto();
            dto.setUserId(userId);
            dto.setNotificationType(NotificationType.type1);
            dto.setMessage("Concurrent test");

            return mockMvc.perform(post("/api/v1/notify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andReturn()
                    .getResponse()
                    .getStatus();
        };

        var futures = executor.invokeAll(
                java.util.stream.Stream.generate(() -> task)
                        .limit(threadCount)
                        .toList()
        );

        for (Future<Integer> future : futures) {
            assertThat(future.get()).isEqualTo(200);
        }

        executor.shutdown();
    }
}