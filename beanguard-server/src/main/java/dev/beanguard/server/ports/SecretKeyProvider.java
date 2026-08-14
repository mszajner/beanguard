package dev.beanguard.server.ports;

import dev.beanguard.server.models.ParameterName;

import javax.crypto.SecretKey;

public interface SecretKeyProvider {
    SecretKey getSecretKey(ParameterName secretKeyParamName);
}
