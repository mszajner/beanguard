package io.beanguard.client.aspects;

import io.beanguard.client.annotations.DecreasesLicenceLimit;
import io.beanguard.client.usage.UsageRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
@RequiredArgsConstructor
public final class LicenceLimitDecreaseAspect {
    private final UsageRegistry usageRegistry;

    @After("@annotation(decreaseLicenceLimit)")
    public void decrementLimit(DecreasesLicenceLimit decreaseLicenceLimit) {
        usageRegistry.decrementUsage(decreaseLicenceLimit.value());
    }
}
