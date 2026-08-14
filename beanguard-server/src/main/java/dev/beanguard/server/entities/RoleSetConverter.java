package dev.beanguard.server.entities;

import dev.beanguard.server.models.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Converter(autoApply = true)
@RequiredArgsConstructor
public class RoleSetConverter implements AttributeConverter<Set<Role>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Set<String>> TYPE_REF = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Set<Role> attribute) {
        if (attribute == null) {
            return null;
        }
        return MAPPER.writeValueAsString(attribute.stream().map(Role::name).collect(Collectors.toSet()));
    }

    @Override
    public Set<Role> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return MAPPER.readValue(dbData, TYPE_REF).stream()
                .map(Role::valueOf)
                .collect(Collectors.toSet());
    }
}
