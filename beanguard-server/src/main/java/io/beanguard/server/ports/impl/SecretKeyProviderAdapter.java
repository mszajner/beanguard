package io.beanguard.server.ports.impl;

import io.beanguard.server.models.ParameterName;
import io.beanguard.server.ports.SecretKeyProvider;
import io.beanguard.server.services.ParameterService;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Log4j2
public class SecretKeyProviderAdapter implements SecretKeyProvider {

    private final ParameterService parameterService;

    @Override
    public SecretKey getSecretKey(ParameterName secretKeyParamName) {
        String secretKeyAsString = parameterService.getString(secretKeyParamName);
        if (StringUtils.isEmpty(secretKeyAsString)) {
            SecretKey secretKey = Jwts.ENC.A256GCM.key().build();
            secretKeyAsString = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            parameterService.setString(secretKeyParamName, secretKeyAsString);
            log.info("Successfully generated and saved new secret key ({}).", secretKeyParamName.name());
            return secretKey;
        }
        return new SecretKeySpec(Base64.getDecoder().decode(secretKeyAsString), "AES");
    }
}
