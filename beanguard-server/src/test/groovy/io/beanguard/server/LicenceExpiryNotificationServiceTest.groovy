package io.beanguard.server

import io.beanguard.api.models.licence.LicenceType
import io.beanguard.server.entities.LicenceEntity
import io.beanguard.server.models.ParameterName
import io.beanguard.server.repositories.LicenceRepository
import io.beanguard.server.services.MailService
import io.beanguard.server.services.ParameterService
import io.beanguard.server.services.impl.LicenceExpiryNotificationService
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class LicenceExpiryNotificationServiceTest extends Specification {

    LicenceRepository licenceRepository = Mock()
    MailService mailService = Mock()
    ParameterService parameterService = Mock()
    LicenceExpiryNotificationService service

    def setup() {
        parameterService.getString(ParameterName.MAIL_EXPIRY_WARNING_DAYS) >> "30,7"
        service = new LicenceExpiryNotificationService(licenceRepository, mailService, parameterService)
    }

    def "sendExpiryWarnings sends warning for each licence in 30-day window"() {
        given:
        def licence = new LicenceEntity()
        licence.email = "owner@example.com"
        licence.companyName = "Acme"
        licence.type = LicenceType.STANDARD
        licence.expiration = Instant.now().atZone(ZoneId.of("Europe/Warsaw"))
            .toLocalDate().plusDays(30).atStartOfDay(ZoneId.of("Europe/Warsaw")).toInstant()
        // first call (30-day window) returns the licence, second call (7-day window) returns empty
        licenceRepository.findByExpirationBetween(_, _) >>> [[licence], []]

        when:
        service.sendExpiryWarnings()

        then:
        1 * mailService.sendExpiryWarning("owner@example.com", "Acme", licence.expiration, 30, LicenceType.STANDARD)
        0 * mailService.sendExpiryWarning(_, _, _, 7, _)
    }

    def "sendExpiryWarnings skips licences with blank email"() {
        given:
        def licence = new LicenceEntity()
        licence.email = ""
        licence.type = LicenceType.STANDARD
        licence.expiration = Instant.now().plus(30, ChronoUnit.DAYS)
        licenceRepository.findByExpirationBetween(_, _) >> [licence]

        when:
        service.sendExpiryWarnings()

        then:
        0 * mailService.sendExpiryWarning(_, _, _, _, _)
    }

    def "sendExpiryWarnings skips licences with null email"() {
        given:
        def licence = new LicenceEntity()
        licence.email = null
        licence.type = LicenceType.DEMO
        licence.expiration = Instant.now().plus(7, ChronoUnit.DAYS)
        licenceRepository.findByExpirationBetween(_, _) >> [licence]

        when:
        service.sendExpiryWarnings()

        then:
        0 * mailService.sendExpiryWarning(_, _, _, _, _)
    }

    def "sendExpiryWarnings uses default thresholds when MAIL_EXPIRY_WARNING_DAYS is invalid"() {
        given:
        parameterService.getString(ParameterName.MAIL_EXPIRY_WARNING_DAYS) >> "bad,value"
        service = new LicenceExpiryNotificationService(licenceRepository, mailService, parameterService)

        when:
        service.sendExpiryWarnings()

        then:
        // default is "30,7" — two thresholds → repository called twice, both return empty
        2 * licenceRepository.findByExpirationBetween(_, _) >> []
        noExceptionThrown()
    }
}
