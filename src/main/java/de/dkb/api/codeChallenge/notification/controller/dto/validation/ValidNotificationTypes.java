package de.dkb.api.codeChallenge.notification.controller.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static de.dkb.api.codeChallenge.notification.common.ApiMessages.INVALID_NOTIFICATION_TYPE;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotificationTypesValidator.class)
@Documented
public @interface ValidNotificationTypes {


    String message() default INVALID_NOTIFICATION_TYPE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
