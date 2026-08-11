package io.beanguard.client.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AopIntegrityCheckerTest {

    @Test
    void failsWhenAopBypassed() {
        // InternalLicenceCheck without Spring proxy — simulates missing AOP
        InternalLicenceCheck rawBean = new InternalLicenceCheck();
        AopIntegrityChecker checker = new AopIntegrityChecker(rawBean);

        assertThatThrownBy(checker::afterSingletonsInstantiated)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("BeanGuard");
    }
}
