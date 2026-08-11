package io.beanguard.server.services.impl;

import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.util.Base64;

@Component
public class KeyMaterialGenerator {

    public record KeyMaterial(String publicKeyBase64, String privateKeyBase64, String secretKeyBase64) {}

    public KeyMaterial generate() {
        KeyPair keyPair = Jwts.SIG.RS256.keyPair().build();
        SecretKey secretKey = Jwts.ENC.A256GCM.key().build();
        return new KeyMaterial(
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()),
                Base64.getEncoder().encodeToString(secretKey.getEncoded())
        );
    }
}
