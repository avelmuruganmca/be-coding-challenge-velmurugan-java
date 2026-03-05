package de.dkb.api.codeChallenge.notification.controller.dto.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import java.io.IOException;
import java.util.UUID;

import static de.dkb.api.codeChallenge.notification.common.ApiMessages.INVALID_UUID_MESSAGE;

public class UuidDeserializer extends JsonDeserializer<UUID> {



    @Override
    public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            throw InvalidFormatException.from(p, INVALID_UUID_MESSAGE, value, UUID.class);
        }
    }
}
