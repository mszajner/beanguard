package io.beanguard.client.aspects;

import io.beanguard.client.annotations.RequiresValidLicence;
import io.beanguard.client.exceptions.MissingOrInvalidLicence;
import io.beanguard.client.registries.LicenceRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
@RequiredArgsConstructor
public final class LicenceValidationAspect {

    private final LicenceRegistry licenceRegistry;

    @Before("@annotation(requiresValidLicence)")
    public void validateLicense(RequiresValidLicence requiresValidLicence) {
        if (!licenceRegistry.getStatus().isValid()) {
            throw new MissingOrInvalidLicence();
        }
    }
}
