package dev.beanguard.server.services;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceCreateRequest;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.api.models.licence.LicenceType;
import dev.beanguard.api.models.licence.LicenceUpdateRequest;
import dev.beanguard.api.models.shop.ShopLicenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface LicenceService {
    String getLicence(UUID key, String secret);

    String createDemoLicence(LicenceDemoCreateRequest licenceDemoCreateRequest);

    Licence createLicence(LicenceCreateRequest licenceCreateRequest);

    Licence updateLicence(UUID key, LicenceUpdateRequest licence);

    Instant getLicenceIat(UUID key);

    Licence getLicence(UUID key);

    Page<Licence> getLicences(String query, LicenceType type, String expirationStatus, Pageable pageable);

    ShopLicenceContext getShopContext(UUID key);

    byte[] getCertificate(UUID key);
}
