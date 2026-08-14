package dev.beanguard.client.aspects;

import dev.beanguard.client.annotations.RequiresValidLicence;
import dev.beanguard.client.exceptions.MissingOrInvalidLicence;
import dev.beanguard.client.registries.LicenceRegistry;
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
