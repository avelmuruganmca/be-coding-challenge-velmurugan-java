package de.dkb.api.notificationhub.notification.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("API documentation for Notification microservice - DKB Code Challenge")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Notification Service Team")
                                .email("support@notification-service.com")
                                .url("https://dkb.de"))
                        .license(new License()
                                .name("Internal Use")
                                .url("https://dkb.de")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation")
                        .url("https://dkb.de"));
    }
}