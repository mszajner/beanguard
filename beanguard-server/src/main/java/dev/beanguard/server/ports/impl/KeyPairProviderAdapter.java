package dev.beanguard.server.ports.impl;

import dev.beanguard.server.models.ParameterName;
import dev.beanguard.server.ports.KeyPairProvider;
import dev.beanguard.server.services.ParameterService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Log4j2
public class KeyPairProviderAdapter implements KeyPairProvider {

    private final ParameterService parameterService;

    @Override
    public KeyPair getKeyPair(ParameterName publicKeyParamName, ParameterName privateKeyParamName)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyAsString = parameterService.getString(publicKeyParamName);
        String privateKeyAsString = parameterService.getString(privateKeyParamName);
        if (StringUtils.isEmpty(privateKeyAsString) || StringUtils.isEmpty(publicKeyAsString)) {
            KeyPair kp = Jwts.SIG.RS256.keyPair().build();
            privateKeyAsString = Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded());
            parameterService.setString(privateKeyParamName, privateKeyAsString);
            publicKeyAsString = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());
            parameterService.setString(publicKeyParamName, publicKeyAsString);
            log.info("Successfully generated and saved new keys pair ({}, {}).",
                    privateKeyParamName.name(), publicKeyParamName.name());
        }
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyAsString);
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(privateSpec);
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyAsString);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicSpec);
        return new KeyPair(publicKey, privateKey);
    }
}
