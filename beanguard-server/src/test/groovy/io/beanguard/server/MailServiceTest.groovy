package io.beanguard.server

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.GreenMailUtil
import com.icegreen.greenmail.util.ServerSetup
import io.beanguard.api.models.licence.LicenceType
import io.beanguard.api.models.shop.*
import io.beanguard.server.models.ParameterName

import java.math.BigDecimal
import io.beanguard.server.services.ParameterService
import io.beanguard.server.services.impl.SpringMailService
import spock.lang.Specification

import java.time.Instant

class MailServiceTest extends Specification {

    GreenMail greenMail
    ParameterService parameterService = Mock()
    SpringMailService mailService

    def setup() {
        greenMail = new GreenMail(new ServerSetup(0, "127.0.0.1", ServerSetup.PROTOCOL_SMTP))
        greenMail.start()
        mailService = new SpringMailService(parameterService)
    }

    def cleanup() {
        greenMail.stop()
    }

    def "sendDemoWelcome sends email with token in body"() {
        given:
        def port = greenMail.smtp.port
        parameterService.getString(ParameterName.MAIL_HOST) >> "127.0.0.1"
        parameterService.getString(ParameterName.MAIL_PORT) >> port.toString()
        parameterService.getString(ParameterName.MAIL_USER) >> "user"
        parameterService.getString(ParameterName.MAIL_PASS) >> "pass"
        parameterService.getString(ParameterName.MAIL_FROM) >> "noreply@test.com"
        parameterService.getString(ParameterName.MAIL_SMTP_STARTTLS) >> "false"
        parameterService.getString(ParameterName.MAIL_DEMO_WELCOME_SUBJECT) >> "Demo licence"
        parameterService.getString(ParameterName.MAIL_DEMO_WELCOME_BODY) >> "Token: {{licenceToken}} Exp: {{expirationDate}}"
        greenMail.setUser("user", "pass")

        when:
        mailService.sendDemoWelcome("customer@example.com", "my-token-123", Instant.parse("2026-12-31T10:00:00Z"))

        then:
        greenMail.receivedMessages.length == 1
        greenMail.receivedMessages[0].subject == "Demo licence"
        GreenMailUtil.getBody(greenMail.receivedMessages[0]).contains("my-token-123")
        GreenMailUtil.getBody(greenMail.receivedMessages[0]).contains("31.12.2026")
    }

    def "sendDemoWelcome does nothing when MAIL_HOST is blank"() {
        given:
        parameterService.getString(ParameterName.MAIL_HOST) >> ""

        when:
        mailService.sendDemoWelcome("x@example.com", "token", Instant.now())

        then:
        greenMail.receivedMessages.length == 0
        noExceptionThrown()
    }

    def "sendExpiryWarning sends email with days and company name"() {
        given:
        def port = greenMail.smtp.port
        parameterService.getString(ParameterName.MAIL_HOST) >> "127.0.0.1"
        parameterService.getString(ParameterName.MAIL_PORT) >> port.toString()
        parameterService.getString(ParameterName.MAIL_USER) >> "user"
        parameterService.getString(ParameterName.MAIL_PASS) >> "pass"
        parameterService.getString(ParameterName.MAIL_FROM) >> "noreply@test.com"
        parameterService.getString(ParameterName.MAIL_SMTP_STARTTLS) >> "false"
        parameterService.getString(ParameterName.MAIL_EXPIRY_WARNING_SUBJECT) >> "Expiry warning"
        parameterService.getString(ParameterName.MAIL_EXPIRY_WARNING_BODY) >> "{{daysLeft}} days for {{email}} ({{buyerCompany}})"
        greenMail.setUser("user", "pass")

        when:
        mailService.sendExpiryWarning("owner@example.com", "Acme Corp", Instant.parse("2026-12-31T10:00:00Z"), 30, LicenceType.STANDARD)

        then:
        greenMail.receivedMessages.length == 1
        GreenMailUtil.getBody(greenMail.receivedMessages[0]).contains("30 days")
        GreenMailUtil.getBody(greenMail.receivedMessages[0]).contains("owner@example.com")
        GreenMailUtil.getBody(greenMail.receivedMessages[0]).contains("Acme Corp")
    }

    def "sendOrderConfirmation attaches PDF to email"() {
        given:
        def port = greenMail.smtp.port
        parameterService.getString(ParameterName.MAIL_HOST) >> "127.0.0.1"
        parameterService.getString(ParameterName.MAIL_PORT) >> port.toString()
        parameterService.getString(ParameterName.MAIL_USER) >> "user"
        parameterService.getString(ParameterName.MAIL_PASS) >> "pass"
        parameterService.getString(ParameterName.MAIL_FROM) >> "noreply@test.com"
        parameterService.getString(ParameterName.MAIL_SENDER_NAME) >> "BeanGuard"
        parameterService.getString(ParameterName.MAIL_SMTP_STARTTLS) >> "false"
        parameterService.getString(ParameterName.MAIL_ORDER_CONFIRM_SUBJECT) >> "Zamowienie #{{orderId}}"
        parameterService.getString(ParameterName.MAIL_ORDER_CONFIRM_BODY) >> "<p>Zamowienie</p>"
        parameterService.getString(ParameterName.LEGAL_VAT_RATE) >> "23"
        parameterService.getString(ParameterName.LEGAL_COMPANY_NAME) >> ""
        parameterService.getString(ParameterName.LEGAL_COMPANY_NIP) >> ""
        parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS) >> ""
        parameterService.getString(ParameterName.BRANDING_SUPPORT_EMAIL) >> ""
        parameterService.getString(ParameterName.LEGAL_CURRENCY) >> "PLN"
        greenMail.setUser("user", "pass")

        when:
        mailService.sendOrderConfirmation(
                "buyer@example.com",
                buildTestOrder(),
                "fake-pdf-bytes".bytes,
                "PF/2026/001")

        then:
        greenMail.receivedMessages.length == 1
        def msg = greenMail.receivedMessages[0]
        msg.contentType.startsWith("multipart/")
        def multipart = (jakarta.mail.Multipart) msg.getContent()
        (0..<multipart.count).any { i ->
            def part = multipart.getBodyPart(i)
            part.fileName != null && part.fileName.startsWith("faktura-proforma")
        }
    }

    private Order buildTestOrder() {
        new Order(UUID.randomUUID(), OrderStatus.NEW, null, OrderPeriod.ONE_YEAR,
                "Test Corp", "ul. Testowa 1", "00-001", "Warszawa",
                "1234567890", "buyer@example.com", null,
                [], BigDecimal.ZERO, Instant.now(), Instant.now(), null, null)
    }
}
