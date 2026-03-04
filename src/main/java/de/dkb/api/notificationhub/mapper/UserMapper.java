package de.dkb.api.notificationhub.mapper;

import de.dkb.api.notificationhub.entity.User;
import de.dkb.api.notificationhub.model.dto.response.RegisterUserResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between User entity (persistence layer) and API DTOs.
 * Keeps controller/API layer decoupled from entity structure.
 */
@Component
public class UserMapper {

    public RegisterUserResponse toRegisterUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new RegisterUserResponse(
                user.getId(),
                user.getNotificationTypeSet()
        );
    }
}
