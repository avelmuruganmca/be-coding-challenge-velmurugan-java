package de.dkb.api.codeChallenge.notification.service;

import de.dkb.api.codeChallenge.notification.controller.dto.request.NotificationRequest;
import de.dkb.api.codeChallenge.notification.controller.dto.request.RegisterUserRequest;
import de.dkb.api.codeChallenge.notification.controller.dto.response.RegisterUserResponse;
import de.dkb.api.codeChallenge.notification.controller.exception.UserNotFoundException;
import de.dkb.api.codeChallenge.notification.domain.NotificationType;
import de.dkb.api.codeChallenge.notification.entity.User;
import de.dkb.api.codeChallenge.notification.mapper.UserMapper;
import de.dkb.api.codeChallenge.notification.repository.UserRepository;
import de.dkb.api.codeChallenge.notification.service.processor.NotificationProcessor;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

    private UserRepository userRepository;
    private NotificationProcessor processor;
    private UserMapper userMapper;
    private MeterRegistry meterRegistry;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        processor = mock(NotificationProcessor.class);
        userMapper = mock(UserMapper.class);
        meterRegistry = new SimpleMeterRegistry();
        service = new NotificationService(userRepository, processor, userMapper, meterRegistry);
    }

    @Nested
    @DisplayName("registerUser")
    class RegisterUserTests {

        @Test
        @DisplayName("should register user and return response with userId and notification types")
        void shouldRegisterUserSuccessfully() {
            UUID userId = UUID.randomUUID();
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .userId(userId)
                    .notifications(List.of("type1", "type2"))
                    .build();
            User savedUser = new User(userId, "type1;type2");
            RegisterUserResponse expectedResponse = new RegisterUserResponse(userId, java.util.Set.of(NotificationType.type1, NotificationType.type2));

            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toRegisterUserResponse(savedUser)).thenReturn(expectedResponse);

            RegisterUserResponse result = service.registerUser(request);

            assertThat(result).isEqualTo(expectedResponse);
            assertThat(result.userId()).isEqualTo(userId);
            assertThat(result.registeredTypes()).containsExactlyInAnyOrder(NotificationType.type1, NotificationType.type2);
            verify(userRepository).save(argThat(user ->
                    user.getId().equals(userId) && "type1;type2".equals(user.getNotifications())
            ));
        }

        @Test
        @DisplayName("should join notifications with semicolon")
        void shouldJoinNotificationsWithSemicolon() {
            UUID userId = UUID.randomUUID();
            RegisterUserRequest request = RegisterUserRequest.builder()
                    .userId(userId)
                    .notifications(List.of("type1", "type2", "type3"))
                    .build();
            User savedUser = new User(userId, "type1;type2;type3");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toRegisterUserResponse(savedUser)).thenReturn(new RegisterUserResponse(userId, java.util.Set.of()));

            service.registerUser(request);

            verify(userRepository).save(argThat(user -> "type1;type2;type3".equals(user.getNotifications())));
        }
    }

    @Nested
    @DisplayName("sendNotification")
    class SendNotificationTests {

        @Test
        @DisplayName("should deliver notification when user exists and is subscribed")
        void shouldSendNotificationSuccessfully() {
            UUID userId = UUID.randomUUID();
            User user = new User(userId, "type1;type2");
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setNotificationType(NotificationType.type1);
            request.setMessage("Hello");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            doNothing().when(processor).process(user, NotificationType.type1, "Hello");

            service.sendNotification(request);

            verify(processor).process(user, NotificationType.type1, "Hello");
            verify(userRepository).findById(userId);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            UUID userId = UUID.randomUUID();
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setNotificationType(NotificationType.type1);
            request.setMessage("Test");

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.sendNotification(request))
                    .isInstanceOf(UserNotFoundException.class);

            verify(processor, never()).process(any(), any(), any());
        }

        @Test
        @DisplayName("should propagate exception and increment error counter when processor throws")
        void shouldPropagateExceptionWhenProcessorFails() {
            UUID userId = UUID.randomUUID();
            User user = new User(userId, "type1");
            NotificationRequest request = new NotificationRequest();
            request.setUserId(userId);
            request.setNotificationType(NotificationType.type1);
            request.setMessage("Fail");

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            doThrow(new RuntimeException("Processor error")).when(processor).process(any(), any(), any());

            assertThatThrownBy(() -> service.sendNotification(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Processor error");

            verify(processor).process(user, NotificationType.type1, "Fail");
        }
    }
}
