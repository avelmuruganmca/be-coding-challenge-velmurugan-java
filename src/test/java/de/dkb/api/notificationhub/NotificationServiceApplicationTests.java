package de.dkb.api.notificationhub;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Application-level smoke tests to verify context loads and core beans are wired.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("NotificationServiceApplication")
class NotificationServiceApplicationTests {

    @Autowired
    private ApplicationContext context;


    @Test
    @DisplayName("should load application context successfully")
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    @DisplayName("should run under test profile")
    void shouldRunUnderTestProfile() {
        String[] profiles = context.getEnvironment().getActiveProfiles();
        assertThat(profiles).contains("test");
    }
}