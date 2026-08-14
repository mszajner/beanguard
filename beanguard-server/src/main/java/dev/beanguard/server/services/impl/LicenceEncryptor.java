package dev.beanguard.server.services.impl;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.server.models.ParameterName;
import dev.beanguard.server.ports.KeyPairProvider;
import dev.beanguard.server.ports.SecretKeyProvider;
import dev.beanguard.server.services.ParameterService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwe;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class LicenceEncryptor {

    private final ParameterService parameterService;
    private final KeyPairProvider keyPairProvider;
    private final SecretKeyProvider secretKeyProvider;

    public String encryptLicence(Licence licence) {
        String issuer = parameterService.getString(ParameterName.LICENCE_ISSUER);
        PrivateKey privateKey = loadKeyPair().getPrivate();
        SecretKey secretKey = secretKeyProvider.getSecretKey(ParameterName.LICENCE_SECRET_KEY);

        Map<String, String> claims = licence.getClaims() != null
                ? new HashMap<>(licence.getClaims())
                : new HashMap<>();
        claims.put("secret", licence.getSecret());
        claims.put("companyName", licence.getCompanyName());
        claims.put("street", licence.getStreet());
        claims.put("postCode", licence.getPostCode());
        claims.put("city", licence.getCity());
        claims.put("vatId", licence.getVatId());
        claims.put("email", licence.getEmail());
        claims.put("phoneNumber", licence.getPhoneNumber());
        String signedToken = Jwts.builder()
                .subject(licence.getKey().toString())
                .issuedAt(Date.from(licence.getUpdatedAt()))
                .expiration(Date.from(licence.getExpiration()))
                .id(UUID.randomUUID().toString())
                .issuer(issuer)
                .claims(claims)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        return Jwts.builder()
                .content(signedToken, "text/plain")
                .encryptWith(secretKey, Jwts.ENC.A256GCM)
                .compact();
    }

    public Licence decryptLicence(String encryptedLicence) {
        SecretKey secretKey = secretKeyProvider.getSecretKey(ParameterName.LICENCE_SECRET_KEY);
        PublicKey publicKey = loadKeyPair().getPublic();

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

    private KeyPair loadKeyPair() {
        try {
            return keyPairProvider.getKeyPair(ParameterName.LICENCE_PUBLIC_KEY, ParameterName.LICENCE_PRIVATE_KEY);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to load licence signing key pair", e);
        }
    }
}
