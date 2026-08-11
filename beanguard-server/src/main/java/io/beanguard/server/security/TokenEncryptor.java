package io.beanguard.server.security;

import io.beanguard.server.models.ParameterName;
import io.beanguard.server.ports.KeyPairProvider;
import io.beanguard.server.ports.SecretKeyProvider;
import io.beanguard.server.services.ParameterService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class TokenEncryptor {

    private final ParameterService parameterService;
    private final KeyPairProvider keyPairProvider;
    private final SecretKeyProvider secretKeyProvider;

    public String generateToken(UserDetails userDetails) {
        String issuer = parameterService.getString(ParameterName.TOKEN_ISSUER);
        PrivateKey privateKey = loadKeyPair().getPrivate();
        SecretKey secretKey = secretKeyProvider.getSecretKey(ParameterName.TOKEN_SECRET_KEY);

        String signedToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + loadExpiration()))
                .id(UUID.randomUUID().toString())
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
        return Jwts.builder()
                .content(signedToken, "text/plain")
                .encryptWith(secretKey, Jwts.ENC.A256GCM)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpirationDate(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        String issuer = parameterService.getString(ParameterName.TOKEN_ISSUER);
        SecretKey secretKey = secretKeyProvider.getSecretKey(ParameterName.TOKEN_SECRET_KEY);
        PublicKey publicKey = loadKeyPair().getPublic();

        String signedJwt = new String(Jwts.parser()
                .requireIssuer(issuer)
                .decryptWith(secretKey)
                .build()
                .parseEncryptedContent(token)
                .getPayload());
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(signedJwt)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpirationDate(token).before(new Date());
    }

    private long loadExpiration() {
        String expirationAsString = parameterService.getString(ParameterName.TOKEN_EXPIRATION);
        if (StringUtils.isEmpty(expirationAsString) || Long.parseLong(expirationAsString) <= 0) {
            expirationAsString = ParameterName.TOKEN_EXPIRATION.getDefaultValue();
            parameterService.setString(ParameterName.TOKEN_EXPIRATION, expirationAsString);
        }
        return Long.parseLong(expirationAsString);
    }

    private KeyPair loadKeyPair() {
        try {
            return keyPairProvider.getKeyPair(ParameterName.TOKEN_PUBLIC_KEY, ParameterName.TOKEN_PRIVATE_KEY);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Failed to load token signing key pair", e);
        }
    }
}
