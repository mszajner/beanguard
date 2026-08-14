package dev.beanguard.client.internal;

import dev.beanguard.client.exceptions.MissingOrInvalidLicence;
import org.springframework.beans.factory.SmartInitializingSingleton;

public class AopIntegrityChecker implements SmartInitializingSingleton {

    private final InternalLicenceCheck licenceCheck;

    public AopIntegrityChecker(InternalLicenceCheck licenceCheck) {
        this.licenceCheck = licenceCheck;
    }

    @Override
    public void afterSingletonsInstantiated() {
        try {
            licenceCheck.verify();
            throw new IllegalStateException(
                "BeanGuard: licence enforcement is not active. " +
                "Ensure Spring AOP proxying is enabled (spring.aop.proxy-target-class=true).");
        } catch (MissingOrInvalidLicence expected) {
            // AOP works correctly — aspect intercepted the call
        }
    }
}
