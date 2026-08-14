package dev.beanguard.client.aspects;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.client.annotations.RequiresLicenceFeature;
import dev.beanguard.client.exceptions.MissingLicenceFeature;
import dev.beanguard.client.registries.LicenceRegistry;
import dev.beanguard.client.registries.LicenceStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LicenceFeatureValidationAspectTest {

    private static RequiresLicenceFeature annotation(String featureName) {
        RequiresLicenceFeature ann = mock(RequiresLicenceFeature.class);
        when(ann.value()).thenReturn(featureName);
        return ann;
    }

    @Test
    void allowsWhenLicenceLoadedAndFeatureTrue() {
        LicenceRegistry registry = mock(LicenceRegistry.class);
        when(registry.getStatus()).thenReturn(LicenceStatus.LOADED);
        when(registry.getLicence()).thenReturn(
            Licence.builder().claims(Map.of("pdf-export", "true")).build());

        LicenceFeatureValidationAspect aspect = new LicenceFeatureValidationAspect(registry);

        assertThatNoException().isThrownBy(() -> aspect.validateFeature(annotation("pdf-export")));
    }

    @Test
    void throwsWhenFeatureValueIsFalse() {
        LicenceRegistry registry = mock(LicenceRegistry.class);
        when(registry.getStatus()).thenReturn(LicenceStatus.LOADED);
        when(registry.getLicence()).thenReturn(
            Licence.builder().claims(Map.of("pdf-export", "false")).build());

        LicenceFeatureValidationAspect aspect = new LicenceFeatureValidationAspect(registry);

        assertThatThrownBy(() -> aspect.validateFeature(annotation("pdf-export")))
            .isInstanceOf(MissingLicenceFeature.class)
            .hasMessage("pdf-export");
    }

    @Test
    void throwsWhenFeatureKeyMissingFromClaims() {
        LicenceRegistry registry = mock(LicenceRegistry.class);
        when(registry.getStatus()).thenReturn(LicenceStatus.LOADED);
        when(registry.getLicence()).thenReturn(
            Licence.builder().claims(Map.of("other-feature", "true")).build());

        LicenceFeatureValidationAspect aspect = new LicenceFeatureValidationAspect(registry);

        assertThatThrownBy(() -> aspect.validateFeature(annotation("pdf-export")))
            .isInstanceOf(MissingLicenceFeature.class)
            .hasMessage("pdf-export");
    }

    @Test
    void throwsWhenClaimsIsNull() {
        LicenceRegistry registry = mock(LicenceRegistry.class);
        when(registry.getStatus()).thenReturn(LicenceStatus.LOADED);
        when(registry.getLicence()).thenReturn(
            Licence.builder().build());

        LicenceFeatureValidationAspect aspect = new LicenceFeatureValidationAspect(registry);

        assertThatThrownBy(() -> aspect.validateFeature(annotation("pdf-export")))
            .isInstanceOf(MissingLicenceFeature.class)
            .hasMessage("pdf-export");
    }

    @Test
    void throwsWhenLicenceNotLoaded() {
        LicenceRegistry registry = mock(LicenceRegistry.class);
        when(registry.getStatus()).thenReturn(LicenceStatus.NOT_LOADED);

        LicenceFeatureValidationAspect aspect = new LicenceFeatureValidationAspect(registry);

        assertThatThrownBy(() -> aspect.validateFeature(annotation("pdf-export")))
            .isInstanceOf(MissingLicenceFeature.class)
            .hasMessage("pdf-export");
    }

    @Test
    void throwsWhenLicenceExpired() {
        LicenceRegistry registry = mock(LicenceRegistry.class);
        when(registry.getStatus()).thenReturn(LicenceStatus.EXPIRED);

        LicenceFeatureValidationAspect aspect = new LicenceFeatureValidationAspect(registry);

        assertThatThrownBy(() -> aspect.validateFeature(annotation("pdf-export")))
            .isInstanceOf(MissingLicenceFeature.class)
            .hasMessage("pdf-export");
    }
}
