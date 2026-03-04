package de.dkb.api.notificationhub.model.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotificationTypesValidator.class)
@Documented
public @interface ValidNotificationTypes {
    String message() default "Invalid notification type.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
