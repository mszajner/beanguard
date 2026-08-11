package io.beanguard.server.services.impl;

import io.beanguard.server.models.KeyRotationResult;
import io.beanguard.server.models.ParameterName;
import io.beanguard.server.ports.InstantProvider;
import io.beanguard.server.services.LicenceKeyService;
import io.beanguard.server.services.ParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class LicenceKeyServiceImpl implements LicenceKeyService {

    private final ParameterService parameterService;
    private final InstantProvider instantProvider;
    private final KeyMaterialGenerator keyMaterialGenerator;

    @Override
    @Transactional
    public KeyRotationResult regenerateKeys() {
        KeyMaterialGenerator.KeyMaterial material = keyMaterialGenerator.generate();

        parameterService.setString(ParameterName.LICENCE_PUBLIC_KEY, material.publicKeyBase64());
        parameterService.setString(ParameterName.LICENCE_PRIVATE_KEY, material.privateKeyBase64());
        parameterService.setString(ParameterName.LICENCE_SECRET_KEY, material.secretKeyBase64());

        log.info("Licence signing/encryption keys regenerated.");

        return new KeyRotationResult(material.publicKeyBase64(), material.secretKeyBase64(), instantProvider.now());
    }
}
