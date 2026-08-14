package dev.beanguard.server.services.impl;

import dev.beanguard.server.entities.ParameterEntity;
import dev.beanguard.server.models.Parameter;
import dev.beanguard.server.models.ParameterName;
import dev.beanguard.server.repositories.ParameterRepository;
import dev.beanguard.server.services.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParameterServiceImpl implements ParameterService {

    private static final Set<ParameterName> WRITE_ONLY = Set.of(
            ParameterName.LICENCE_PRIVATE_KEY,
            ParameterName.TOKEN_PRIVATE_KEY,
            ParameterName.MAIL_PASS
    );

    private final ParameterRepository parameterRepository;
    private final ObjectMapper objectMapper;

    @Override
    public String getString(ParameterName parameterName) {
        return parameterRepository.findById(parameterName.name())
                .map(ParameterEntity::getValue)
                .orElse(parameterName.getDefaultValue());
    }

    @Override
    public void setString(ParameterName parameterName, String value) {
        ParameterEntity parameter = parameterRepository.findById(parameterName.name())
                .orElseGet(ParameterEntity::new);
        parameter.setName(parameterName.name());
        parameter.setValue(value);
        parameterRepository.save(parameter);
    }

    @Override
    public <T> T getObject(ParameterName parameterName, TypeReference<T> typeReference) {
        return objectMapper.readValue(getString(parameterName), typeReference);
    }

    @Override
    public List<Parameter> getAllParameters() {
        Map<String, ParameterEntity> existing = parameterRepository.findAll()
                .stream()
                .collect(Collectors.toMap(ParameterEntity::getName, Function.identity()));
        return Arrays.stream(ParameterName.values())
                .map(pn -> toParameter(pn, existing.get(pn.name())))
                .toList();
    }

    @Override
    public Parameter getParameter(ParameterName parameterName) {
        ParameterEntity entity = parameterRepository.findById(parameterName.name()).orElse(null);
        return toParameter(parameterName, entity);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public Parameter updateAndGet(ParameterName parameterName, String value) {
        setString(parameterName, value);
        return getParameter(parameterName);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public void resetParameter(ParameterName parameterName) {
        parameterRepository.deleteById(parameterName.name());
    }

    private Parameter toParameter(ParameterName pn, ParameterEntity entity) {
        String value = WRITE_ONLY.contains(pn) ? null
                : entity != null ? entity.getValue() : pn.getDefaultValue();
        Instant createdAt = entity != null ? entity.getCreatedAt() : null;
        Instant updatedAt = entity != null ? entity.getUpdatedAt() : null;
        return new Parameter(pn.name(), value, createdAt, updatedAt);
    }
}
