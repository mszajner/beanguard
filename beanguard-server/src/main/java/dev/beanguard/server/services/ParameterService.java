package dev.beanguard.server.services;

import dev.beanguard.server.models.Parameter;
import dev.beanguard.server.models.ParameterName;
import tools.jackson.core.type.TypeReference;

import java.util.List;

public interface ParameterService {
    String getString(ParameterName parameterName);

    void setString(ParameterName parameterName, String value);

    <T> T getObject(ParameterName parameterName, TypeReference<T> typeReference);

    List<Parameter> getAllParameters();

    Parameter getParameter(ParameterName parameterName);

    Parameter updateAndGet(ParameterName parameterName, String value);

    void resetParameter(ParameterName parameterName);
}
