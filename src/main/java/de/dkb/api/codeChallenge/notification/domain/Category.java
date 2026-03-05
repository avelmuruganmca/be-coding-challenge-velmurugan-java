package de.dkb.api.codeChallenge.notification.domain;

import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

@Getter
public enum Category {

    CATEGORY_A(Set.of(
            NotificationType.type1,
            NotificationType.type2,
            NotificationType.type3,
            NotificationType.type6
    )),
    CATEGORY_B(Set.of(
            NotificationType.type4,
            NotificationType.type5
    ));

    public static final String UNKNOWN_CATEGORY_FOR_TYPE = "Unknown category for type: ";
    private final Set<NotificationType> types;

    Category(Set<NotificationType> types) {
        this.types = types;
    }

    public static Category from(NotificationType type) {
        return Arrays.stream(values())
                .filter(category -> category.types.contains(type))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(UNKNOWN_CATEGORY_FOR_TYPE + type));
    }

    public static boolean isSupported(NotificationType type) {
        if (type == null) return false;
        try {
            from(type);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}