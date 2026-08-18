package dev.beanguard.server

import dev.beanguard.server.config.RateLimitInterceptor
import dev.beanguard.server.models.ParameterName
import dev.beanguard.server.services.ParameterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.spockframework.spring.EnableSharedInjection
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import spock.lang.Shared
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableSharedInjection
abstract class IntegrationSpec extends Specification {

    @LocalServerPort
    protected int localServerPort

    @Autowired
    RateLimitInterceptor rateLimitInterceptor

    @Shared @Autowired
    ParameterService parameterService

    def setup() {
        rateLimitInterceptor.resetBuckets()
    }

    def setupSpec() {
        parameterService.setString(ParameterName.MAIL_HOST, mailpit.getHost())
        parameterService.setString(ParameterName.MAIL_PORT, mailpit.getMappedPort(1025).toString())
    }

    static PostgreSQLContainer postgres
    static GenericContainer mailpit

    static {
        postgres = new PostgreSQLContainer("postgres:16.11")
        postgres.start()

        // Mirrors the mailpit service in docker-compose.yml — acceptance specs that
        // trigger SpringMailService need a real SMTP endpoint, since MAIL_HOST
        // defaults to the literal hostname "mailpit" (only resolvable in that network).
        mailpit = new GenericContainer("axllent/mailpit:latest")
                .withExposedPorts(1025, 8025)
        mailpit.start()
        println "Mailpit UI (test emails): http://${mailpit.getHost()}:${mailpit.getMappedPort(8025)}"

        Runtime.runtime.addShutdownHook(new Thread({ postgres.stop(); mailpit.stop() }))
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
    }
}
