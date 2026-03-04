package de.dkb.api.notificationhub.repository;

import de.dkb.api.notificationhub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}