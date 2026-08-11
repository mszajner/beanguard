package io.beanguard.client.aspects;

import io.beanguard.client.annotations.RequiresLicenceFeature;
import io.beanguard.client.exceptions.MissingLicenceFeature;
import io.beanguard.client.registries.LicenceRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.Map;

@Aspect
@RequiredArgsConstructor
public final class LicenceFeatureValidationAspect {

    private final LicenceRegistry licenceRegistry;

    @Before("@annotation(requiresLicenceFeature)")
    public void validateFeature(RequiresLicenceFeature requiresLicenceFeature) {
        if (!licenceRegistry.getStatus().isValid()) {
            throw new MissingLicenceFeature(requiresLicenceFeature.value());
        }
        Map<String, String> claims = licenceRegistry.getLicence().getClaims();
        if (claims == null || !"true".equals(claims.getOrDefault(requiresLicenceFeature.value(), "false"))) {
            throw new MissingLicenceFeature(requiresLicenceFeature.value());
        }
    }
}
