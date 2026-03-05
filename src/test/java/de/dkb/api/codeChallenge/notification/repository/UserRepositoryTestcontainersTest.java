package de.dkb.api.codeChallenge.notification.repository;

import de.dkb.api.codeChallenge.notification.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("testcontainers")
@DisplayName("UserRepository Testcontainers Tests")
class UserRepositoryTestcontainersTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("should save and find user by id")
    void shouldSaveAndFindById() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "type1;type2;type3");

        User saved = userRepository.save(user);
        Optional<User> found = userRepository.findById(userId);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(userId);
        assertThat(saved.getNotifications()).isEqualTo("type1;type2;type3");
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(userId);
        assertThat(found.get().getNotifications()).isEqualTo("type1;type2;type3");
    }

    @Test
    @DisplayName("should return empty when user does not exist")
    void shouldReturnEmptyWhenNotFound() {
        Optional<User> found = userRepository.findById(UUID.randomUUID());
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("should update user when saving with same id")
    void shouldUpdateWhenSavingSameId() {
        UUID userId = UUID.randomUUID();
        User user = new User(userId, "type1;type2");
        userRepository.save(user);

        User updated = new User(userId, "type3;type4;type5");
        User saved = userRepository.save(updated);

        Optional<User> found = userRepository.findById(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getNotifications()).isEqualTo("type3;type4;type5");
    }
}
