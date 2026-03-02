package de.dkb.api.codeChallenge.notification.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApplicationMetadata {

    private final String serviceName;

    public ApplicationMetadata(
            @Value("${spring.application.name}") String serviceName) {
        this.serviceName = serviceName;
    }

}