package io.beanguard.client;

import io.beanguard.client.config.BeanGuardConfiguration;
import io.beanguard.client.config.LicenceKeys;
import io.beanguard.client.config.ServerConfig;
import io.beanguard.client.registries.LicenceRegistry;
import io.beanguard.client.registries.LicenceStatus;
import io.beanguard.client.server.BeanGuardServer;
import io.beanguard.client.usage.UsageRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(BeanGuardClientAutoConfiguration.class));

    @Test
    void licenceBeansAreAbsentWithoutServerConfiguration() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(BeanGuardConfiguration.class);
            assertThat(context).doesNotHaveBean(BeanGuardServer.class);
            assertThat(context).doesNotHaveBean(LicenceRegistry.class);
            assertThat(context).hasSingleBean(UsageRegistry.class);
        });
    }

    @Test
    void licenceBeansAreWiredWhenServerConfigurationIsProvided() {
        runner.withUserConfiguration(WithServerConfiguration.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(BeanGuardServer.class);
            assertThat(context).hasSingleBean(LicenceRegistry.class);
            assertThat(context).hasSingleBean(UsageRegistry.class);
        });
    }

    @Test
    void usageRegistryCanBeOverridden() {
        runner.withUserConfiguration(WithServerConfiguration.class, OverrideUsageRegistryConfig.class).run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(UsageRegistry.class)).isInstanceOf(FakeUsageRegistry.class);
        });
    }

    @Test
    void contextFailsWhenLicenceRegistryIsOverridden() {
        runner.withUserConfiguration(WithServerConfiguration.class, OverrideLicenceRegistryConfig.class).run(context ->
            assertThat(context).hasFailed()
        );
    }

    @Configuration
    static class WithServerConfiguration {
        @Bean
        BeanGuardConfiguration beanGuardServerConfiguration() {
            return new FakeServerConfiguration();
        }
    }

    @Configuration
    static class OverrideLicenceRegistryConfig {
        @Bean
        LicenceRegistry fakeLicenceRegistry() {
            return new LicenceRegistry() {
                @Override public LicenceStatus getStatus() {
                    return LicenceStatus.LOADED;
                }
                @Override public io.beanguard.api.models.licence.Licence getLicence() { return null; }
                @Override public long getLimit(String name) { return Long.MAX_VALUE; }
                @Override public void refreshLicence() {}
            };
        }
    }

    @Configuration
    static class OverrideUsageRegistryConfig {
        @Bean
        UsageRegistry fakeUsageRegistry() {
            return new FakeUsageRegistry();
        }
    }

    static class FakeUsageRegistry implements UsageRegistry {
        @Override public long getUsage(String name) { return 0L; }
        @Override public void incrementUsage(String name) {}
        @Override public void decrementUsage(String name) {}
    }

    static class FakeServerConfiguration implements BeanGuardConfiguration {
        @Override
        public ServerConfig getServerConfig() {
            return new ServerConfig();
        }

        @Override
        public Optional<LicenceKeys> getLicenceKeys() {
            return Optional.empty();
        }

        @Override
        public Optional<String> loadLicence() {
            return Optional.empty();
        }

        @Override
        public void saveLicence(String licence) {

        }
    }
}
