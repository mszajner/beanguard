package dev.beanguard.server.services;

import dev.beanguard.server.entities.LicenceEntity;

public interface CertificateService {
    byte[] generate(LicenceEntity licence);
}
