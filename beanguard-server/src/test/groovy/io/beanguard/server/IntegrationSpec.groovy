package io.beanguard.server

import io.beanguard.server.config.RateLimitInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class IntegrationSpec extends Specification {

    @LocalServerPort
    protected int localServerPort

    @Autowired
    RateLimitInterceptor rateLimitInterceptor

    def setup() {
        rateLimitInterceptor.resetBuckets()
    }

    static PostgreSQLContainer postgres

    static {
        postgres = new PostgreSQLContainer("postgres:16.11")
        postgres.start()
        Runtime.runtime.addShutdownHook(new Thread({ postgres.stop() }))
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }
}
