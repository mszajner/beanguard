package io.beanguard.server.mappers;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Converter
public class JsonToMapConverter implements AttributeConverter<Map<String, String>, String> {

    private static final TypeReference<Map<String, String>> TYPE_REF = new TypeReference<>() {};

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        try {
            return attribute == null ? null : objectMapper.writeValueAsString(attribute);
        } catch (JacksonException e) {
            throw new RuntimeException("Error serializing map to JSON", e);
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : objectMapper.readValue(dbData, TYPE_REF);
        } catch (JacksonException e) {
            throw new RuntimeException("Error deserializing JSON to map", e);
        }
    }
}