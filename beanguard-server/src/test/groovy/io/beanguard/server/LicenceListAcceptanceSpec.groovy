package io.beanguard.server

import io.beanguard.api.models.licence.LicenceCreateRequest
import io.beanguard.server.controllers.LicencesController
import io.beanguard.server.services.LicenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional

import java.time.Instant
import java.time.temporal.ChronoUnit

@Transactional
class LicenceListAcceptanceSpec extends IntegrationSpec {

    @Autowired LicenceService licenceService
    @Autowired LicencesController licencesController

    def setup() {
        licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .email("alice@example.com").vatId("8888888881").build())
        licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .email("bob@example.com").vatId("8888888882").build())
        licenceService.createLicence(LicenceCreateRequest.builder()
            .expiration(Instant.now().plus(365, ChronoUnit.DAYS))
            .email("carol@example.com").vatId("8888888883").build())
    }

    def "should return all licences without filter"() {
        given:
        def pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))

        when:
        def page = licencesController.getLicences(pageable, null, null, null)

        then:
        page.totalElements >= 3
        page.content.every { it.secret == null }
    }

    def "should filter by email partial match"() {
        given:
        def pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))

        when:
        def page = licencesController.getLicences(pageable, "alice", null, null)

        then:
        page.content.size() == 1
        page.content[0].email == "alice@example.com"
    }

    def "should filter by vatId partial match"() {
        given:
        def pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))

        when:
        def page = licencesController.getLicences(pageable, "8888888882", null, null)

        then:
        page.content.size() == 1
        page.content[0].vatId == "8888888882"
    }

    def "should paginate results"() {
        given:
        def pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"))

        when:
        def page = licencesController.getLicences(pageable, null, null, null)

        then:
        page.content.size() == 2
        page.totalElements >= 3
        page.totalPages >= 2
    }

    def "should return empty page when nothing matches"() {
        given:
        def pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"))

        when:
        def page = licencesController.getLicences(pageable, "nonexistent-xyz-99@nowhere.com", null, null)

        then:
        page.totalElements == 0
        page.content.isEmpty()
    }
}
