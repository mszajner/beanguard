package io.beanguard.client.aspects;

import io.beanguard.client.annotations.RequiresLicenceLimit;
import io.beanguard.client.exceptions.LicenceLimitExceeded;
import io.beanguard.client.registries.LicenceRegistry;
import io.beanguard.client.usage.UsageRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
@RequiredArgsConstructor
public final class LicenceLimitValidationAspect {
    private final LicenceRegistry licenceRegistry;
    private final UsageRegistry usageRegistry;

    @Before("@annotation(requiresLicenceLimit)")
    public void validateLimit(RequiresLicenceLimit requiresLicenceLimit) {
        long limit = licenceRegistry.getLimit(requiresLicenceLimit.value());
        long usage = usageRegistry.getUsage(requiresLicenceLimit.value());
        if (usage >= limit) {
            throw new LicenceLimitExceeded(requiresLicenceLimit.value() + ":" + usage + ":" + limit);
        }
    }

    @After("@annotation(requiresLicenceLimit)")
    public void incrementLimit(RequiresLicenceLimit requiresLicenceLimit) {
        usageRegistry.incrementUsage(requiresLicenceLimit.value());
    }
}
