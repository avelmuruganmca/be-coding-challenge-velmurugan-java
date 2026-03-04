package de.dkb.api.notificationhub.model.validation;

import de.dkb.api.notificationhub.domain.Category;
import de.dkb.api.notificationhub.domain.NotificationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

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
                        "Notification type '" + item + "' is invalid or not supported"
                ).addConstraintViolation();
                return false;
            }
        }
        return true;
    }
}