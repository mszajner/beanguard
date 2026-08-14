package dev.beanguard.server.ports;

import dev.beanguard.server.models.ParameterName;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public interface KeyPairProvider {
    KeyPair getKeyPair(ParameterName publicKeyParamName, ParameterName privateKeyParamName)
            throws NoSuchAlgorithmException, InvalidKeySpecException;
}
