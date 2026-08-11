package io.beanguard.server

import io.beanguard.api.models.licence.LicenceCreateRequest
import io.beanguard.api.models.licence.LicenceDemoCreateRequest
import io.beanguard.api.models.licence.LicenceType
import io.beanguard.server.exceptions.DemoLicenceAlreadyExistsException
import io.beanguard.server.models.ParameterName
import io.beanguard.server.services.LicenceService
import io.beanguard.server.services.ParameterService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

import java.time.Instant
import java.time.temporal.ChronoUnit

class DemoLicenceAcceptanceSpec extends IntegrationSpec {

    @Autowired LicenceService licenceService
    @Autowired ParameterService parameterService

    def "demo licence should have type DEMO"() {
        given:
        def email = "demo-type-${System.currentTimeMillis()}@test.pl"
        def req = LicenceDemoCreateRequest.builder().email(email).vatId("5632103754").build()

        when:
        licenceService.createDemoLicence(req)
        def pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending())
        def page = licenceService.getLicences(email, LicenceType.DEMO, null, pageable)

        then:
        page.totalElements == 1
        page.content[0].type == LicenceType.DEMO
    }

    def "should reject second demo licence for same email"() {
        given:
        def email = "demo-dup-${System.currentTimeMillis()}@test.pl"
        def req = LicenceDemoCreateRequest.builder().email(email).vatId("5632103754").build()
        licenceService.createDemoLicence(req)

        when:
        licenceService.createDemoLicence(req)

        then:
        thrown(DemoLicenceAlreadyExistsException)
    }

    def "demo expiration should respect LICENCE_DEMO_EXPIRATION_DAYS parameter"() {
        given:
        parameterService.setString(ParameterName.LICENCE_DEMO_EXPIRATION_DAYS, "7")
        def email = "demo-exp-${System.currentTimeMillis()}@test.pl"
        def req = LicenceDemoCreateRequest.builder().email(email).vatId("5632103754").build()

        when:
        licenceService.createDemoLicence(req)
        def pageable = PageRequest.of(0, 1, Sort.by("createdAt").descending())
        def licence = licenceService.getLicences(email, LicenceType.DEMO, null, pageable).content[0]

        then:
        def expected = Instant.now().plus(7, ChronoUnit.DAYS)
        licence.expiration.truncatedTo(ChronoUnit.SECONDS) == expected.truncatedTo(ChronoUnit.SECONDS)

        cleanup:
        parameterService.setString(ParameterName.LICENCE_DEMO_EXPIRATION_DAYS, "30")
    }

    def "getLicences should filter by type"() {
        given:
        def suffix = System.currentTimeMillis()
        licenceService.createDemoLicence(
            LicenceDemoCreateRequest.builder().email("demo-filter-${suffix}@test.pl").vatId("5632103754").build())
        licenceService.createLicence(
            LicenceCreateRequest.builder()
                .email("std-filter-${suffix}@test.pl")
                .vatId("5632103754")
                .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
                .build())
        def pageable = PageRequest.of(0, 100, Sort.by("createdAt").descending())

        expect:
        licenceService.getLicences(null, LicenceType.DEMO, null, pageable).content.every {
            it.type == LicenceType.DEMO
        }
        licenceService.getLicences(null, LicenceType.STANDARD, null, pageable).content.every {
            it.type == LicenceType.STANDARD
        }
    }

    def "createDemoLicence succeeds and returns token when MAIL_HOST is not configured"() {
        // MAIL_HOST is empty by default in test environment — mail is silently skipped
        given:
        def email = "nomail-${System.currentTimeMillis()}@example.com"
        def req = LicenceDemoCreateRequest.builder().email(email).vatId("5632103754").build()

        when:
        def token = licenceService.createDemoLicence(req)

        then:
        token != null
        !token.isBlank()
        noExceptionThrown()
    }
}
