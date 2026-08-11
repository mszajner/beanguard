package io.beanguard.server.services;

import io.beanguard.server.entities.LicenceEntity;

public interface CertificateService {
    byte[] generate(LicenceEntity licence);
}
