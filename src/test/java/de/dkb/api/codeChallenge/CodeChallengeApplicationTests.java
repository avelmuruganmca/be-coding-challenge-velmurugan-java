package de.dkb.api.codeChallenge;

import de.dkb.api.codeChallenge.notification.config.ApplicationMetadata;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CodeChallengeApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ApplicationMetadata metadata;

    @Autowired(required = false)
    private SpringLiquibase liquibase;

    // =========================
    // Context Load Validation
    // =========================
    @Test
    @DisplayName("Application context should load successfully")
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    // =========================
    // Bean Wiring Validation
    // =========================
    @Test
    @DisplayName("Core infrastructure beans should be present")
    void shouldLoadCoreBeans() {

        assertThat(metadata).isNotNull();

        // Liquibase optional but recommended
        assertThat(liquibase).isNotNull();
    }

    // =========================
    // Environment Validation
    // =========================
    @Test
    @DisplayName("Application should run under test profile")
    void shouldRunUnderTestProfile() {

        String[] activeProfiles = context.getEnvironment().getActiveProfiles();

        assertThat(activeProfiles)
                .contains("test");
    }
}