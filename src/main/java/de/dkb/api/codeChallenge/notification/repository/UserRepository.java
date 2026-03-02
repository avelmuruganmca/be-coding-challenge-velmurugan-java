package de.dkb.api.codeChallenge.notification.repository;

import de.dkb.api.codeChallenge.notification.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}