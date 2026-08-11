package io.beanguard.client;

import io.beanguard.client.aspects.LicenceFeatureValidationAspect;
import io.beanguard.client.aspects.LicenceLimitDecreaseAspect;
import io.beanguard.client.aspects.LicenceLimitValidationAspect;
import io.beanguard.client.aspects.LicenceValidationAspect;
import io.beanguard.client.config.BeanGuardConfiguration;
import io.beanguard.client.internal.AopIntegrityChecker;
import io.beanguard.client.internal.InternalLicenceCheck;
import io.beanguard.client.registries.LicenceRegistry;
import io.beanguard.client.registries.LicenceRegistryDefault;
import io.beanguard.client.server.BeanGuardServer;
import io.beanguard.client.server.BeanGuardServerDefault;
import io.beanguard.client.usage.UsageRegistry;
import io.beanguard.client.usage.UsageRegistryDefault;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
public class BeanGuardClientAutoConfiguration {

    @Bean
    @ConditionalOnBean(BeanGuardConfiguration.class)
    public BeanGuardServer beanGuardServer(BeanGuardConfiguration configuration) {
        return new BeanGuardServerDefault(configuration);
    }

    @Bean
    @ConditionalOnBean(BeanGuardServer.class)
    public LicenceRegistry licenceRegistry(BeanGuardServer beanGuardServer, ApplicationEventPublisher publisher) {
        return new LicenceRegistryDefault(beanGuardServer, publisher);
    }

    @Bean
    @ConditionalOnMissingBean(UsageRegistry.class)
    public UsageRegistry usageRegistry() {
        return new UsageRegistryDefault();
    }

    @Bean
    @ConditionalOnBean(LicenceRegistry.class)
    public LicenceValidationAspect licenceValidationAspect(LicenceRegistry licenceRegistry) {
        return new LicenceValidationAspect(licenceRegistry);
    }

    @Bean
    @ConditionalOnBean({LicenceRegistry.class, UsageRegistry.class})
    public LicenceLimitValidationAspect licenceLimitValidationAspect(
            LicenceRegistry licenceRegistry,
            UsageRegistry usageRegistry) {
        return new LicenceLimitValidationAspect(licenceRegistry, usageRegistry);
    }

    @Bean
    @ConditionalOnBean(UsageRegistry.class)
    public LicenceLimitDecreaseAspect licenceLimitDecreaseAspect(UsageRegistry usageRegistry) {
        return new LicenceLimitDecreaseAspect(usageRegistry);
    }

    @Bean
    @ConditionalOnBean(LicenceRegistry.class)
    public LicenceFeatureValidationAspect licenceFeatureValidationAspect(LicenceRegistry licenceRegistry) {
        return new LicenceFeatureValidationAspect(licenceRegistry);
    }

    @Bean
    @ConditionalOnBean(BeanGuardServer.class)
    public InternalLicenceCheck internalLicenceCheck() {
        return new InternalLicenceCheck();
    }

    @Bean
    @ConditionalOnBean(InternalLicenceCheck.class)
    public AopIntegrityChecker aopIntegrityChecker(InternalLicenceCheck internalLicenceCheck) {
        return new AopIntegrityChecker(internalLicenceCheck);
    }

    @Bean
    @ConditionalOnBean(BeanGuardServer.class)
    public static BeanFactoryPostProcessor licenceIntegrityPostProcessor() {
        return beanFactory -> {
            assertSingleBean(beanFactory, LicenceRegistry.class);
            assertSingleBean(beanFactory, UsageRegistry.class);
        };
    }

    private static void assertSingleBean(ConfigurableListableBeanFactory beanFactory, Class<?> type) {
        String[] names = beanFactory.getBeanNamesForType(type, false, false);
        if (names.length > 1) {
            throw new IllegalStateException(
                "BeanGuard: " + type.getSimpleName() + " must not be overridden. " +
                "Remove any custom " + type.getSimpleName() + " beans from the application context.");
        }
    }
}
