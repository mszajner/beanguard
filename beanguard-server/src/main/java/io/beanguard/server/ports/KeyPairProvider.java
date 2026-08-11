package io.beanguard.server.ports;

import io.beanguard.server.models.ParameterName;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface KeyPairProvider {
    KeyPair getKeyPair(ParameterName publicKeyParamName, ParameterName privateKeyParamName)
            throws NoSuchAlgorithmException, InvalidKeySpecException;
}
