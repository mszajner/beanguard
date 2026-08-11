package io.beanguard.server.ports;

import io.beanguard.server.models.ParameterName;

import javax.crypto.SecretKey;

public interface SecretKeyProvider {
    SecretKey getSecretKey(ParameterName secretKeyParamName);
}
