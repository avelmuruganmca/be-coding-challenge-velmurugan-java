package de.dkb.api.codeChallenge.notification.controller.dto.validation;

import de.dkb.api.codeChallenge.notification.domain.Category;
import de.dkb.api.codeChallenge.notification.domain.NotificationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

import static de.dkb.api.codeChallenge.notification.common.ApiMessages.IS_INVALID_OR_NOT_SUPPORTED;
import static de.dkb.api.codeChallenge.notification.common.ApiMessages.NOTIFICATION_TYPE;

public class NotificationTypesValidator implements ConstraintValidator<ValidNotificationTypes, List<String>> {


    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // @NotEmpty handles empty
        }
        for (String item : value) {
            if (item == null || item.isBlank()) {
                return false;
            }
            try {
                NotificationType type = NotificationType.valueOf(item.trim());
                Category.from(type); // throws if not in any category
            } catch (IllegalArgumentException e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        NOTIFICATION_TYPE + item + IS_INVALID_OR_NOT_SUPPORTED
                ).addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}