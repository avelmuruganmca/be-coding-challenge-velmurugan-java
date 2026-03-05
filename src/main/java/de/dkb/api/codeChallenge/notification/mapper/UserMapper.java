package de.dkb.api.codeChallenge.notification.mapper;

import de.dkb.api.codeChallenge.notification.controller.dto.response.RegisterUserResponse;
import de.dkb.api.codeChallenge.notification.entity.User;
import org.springframework.stereotype.Component;

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
