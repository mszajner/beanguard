package dev.beanguard.client.server;

import dev.beanguard.api.models.licence.Licence;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwts;
import lombok.SneakyThrows;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class LicenceDecryptor {

    private final PublicKey publicKey;
    private final SecretKey secretKey;

    @SneakyThrows
    public LicenceDecryptor(String publicKeyAsString, String secretKeyAsString) {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyAsString);
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
        this.publicKey = keyFactory.generatePublic(publicSpec);
        this.secretKey = new SecretKeySpec(
            Base64.getDecoder().decode(secretKeyAsString), "AES");
    }

    public Licence decrypt(String encryptedLicence) {
        Jwe<byte[]> jwe = Jwts.parser()
                .decryptWith(secretKey)
                .build()
                .parseEncryptedContent(encryptedLicence);
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(new String(jwe.getPayload(), StandardCharsets.UTF_8))
                .getPayload();

        Map<String, String> claimsMap = claims.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("sub")
                        && !entry.getKey().equals("iat")
                        && !entry.getKey().equals("exp")
                        && !entry.getKey().equals("jti")
                        && !entry.getKey().equals("iss")
                        && !entry.getKey().equals("secret")
                        && !entry.getKey().equals("companyName")
                        && !entry.getKey().equals("street")
                        && !entry.getKey().equals("postCode")
                        && !entry.getKey().equals("city")
                        && !entry.getKey().equals("vatId")
                        && !entry.getKey().equals("email")
                        && !entry.getKey().equals("phoneNumber")
                )
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));

        return Licence.builder()
                .key(UUID.fromString(claims.getSubject()))
                .secret(claims.get("secret", String.class))
                .expiration(claims.getExpiration().toInstant())
                .companyName(claims.get("companyName", String.class))
                .street(claims.get("street", String.class))
                .postCode(claims.get("postCode", String.class))
                .city(claims.get("city", String.class))
                .vatId(claims.get("vatId", String.class))
                .email(claims.get("email", String.class))
                .phoneNumber(claims.get("phoneNumber", String.class))
                .claims(claimsMap)
                .updatedAt(claims.getIssuedAt().toInstant())
                .build();
    }
}
