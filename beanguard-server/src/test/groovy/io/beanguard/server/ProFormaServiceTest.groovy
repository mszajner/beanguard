package io.beanguard.server

import io.beanguard.server.entities.OrderEntity
import io.beanguard.server.entities.OrderItemEntity
import io.beanguard.server.entities.ProFormaEntity
import io.beanguard.server.models.ParameterName
import io.beanguard.server.repositories.ProFormaRepository
import io.beanguard.server.services.ParameterService
import io.beanguard.server.services.impl.ProFormaNumberAllocator
import io.beanguard.server.services.impl.ProFormaServiceImpl
import spock.lang.Specification

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class ProFormaServiceTest extends Specification {

    ParameterService parameterService = Mock()
    ProFormaNumberAllocator numberAllocator = Mock()
    ProFormaRepository proFormaRepository = Mock()
    ProFormaServiceImpl service = new ProFormaServiceImpl(parameterService, numberAllocator, proFormaRepository)

    def "generate returns entity with number from allocator and order reference"() {
        given:
        def order = buildOrder()
        numberAllocator.allocateNext(2026) >> "PF/2026/001"
        stubParams()
        proFormaRepository.save(_ as ProFormaEntity) >> { ProFormaEntity e -> e }

        when:
        def result = service.generate(order, LocalDate.of(2026, 6, 30), null, LocalDate.of(2027, 6, 30))

        then:
        result.number == "PF/2026/001"
        result.order == order
        result.pdf != null
        result.pdf.length > 0
    }

    def "generate passes year from issueDate to allocator"() {
        given:
        def order = buildOrder()
        stubParams()
        proFormaRepository.save(_ as ProFormaEntity) >> { ProFormaEntity e -> e }

        when:
        service.generate(order, LocalDate.of(2025, 1, 15), null, LocalDate.of(2026, 1, 15))

        then:
        1 * numberAllocator.allocateNext(2025) >> "PF/2025/001"
    }

    def "generate produces non-trivial PDF when order has items"() {
        given:
        def order = buildOrder()
        def item = new OrderItemEntity()
        item.name = "BeanGuard Standard"
        item.price = new BigDecimal("199.00")
        item.quantity = 2
        order.items = [item]
        numberAllocator.allocateNext(_) >> "PF/2026/001"
        stubParams()
        proFormaRepository.save(_ as ProFormaEntity) >> { ProFormaEntity e -> e }

        when:
        def result = service.generate(order, LocalDate.of(2026, 6, 30), null, LocalDate.of(2027, 6, 30))

        then:
        result.pdf.length > 500
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private OrderEntity buildOrder() {
        def o = new OrderEntity()
        o.id = UUID.randomUUID()
        o.vatId = "1234567890"
        o.email = "buyer@example.com"
        o.companyName = "Test Corp Sp. z o.o."
        o.street = "ul. Testowa 1"
        o.postCode = "00-001"
        o.city = "Warszawa"
        o.items = []
        o
    }

    private void stubParams() {
        parameterService.getString(ParameterName.LEGAL_VAT_RATE) >> "23"
        parameterService.getString(ParameterName.LEGAL_PRO_FORMA_PAYMENT_DAYS) >> "14"
        parameterService.getString(ParameterName.LEGAL_CURRENCY) >> "PLN"
        parameterService.getString(ParameterName.LEGAL_COMPANY_NAME) >> "Seller Corp"
        parameterService.getString(ParameterName.LEGAL_COMPANY_NIP) >> "9999999999"
        parameterService.getString(ParameterName.LEGAL_COMPANY_ADDRESS) >> "ul. Sprzedawcy 1, 00-001 Warszawa"
        parameterService.getString(ParameterName.LEGAL_COMPANY_PHONE) >> "+48 123 456 789"
        parameterService.getString(ParameterName.LEGAL_COMPANY_KRS) >> ""
        parameterService.getString(ParameterName.LEGAL_BANK_ACCOUNT) >> ""
        parameterService.getString(ParameterName.LEGAL_PRO_FORMA_TEMPLATE) >> minimalXhtml()
    }

    private static String minimalXhtml() {
        """<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/1999/xhtml">
<head><title>PF {{number}}</title></head>
<body>
<p>FAKTURA PRO-FORMA {{number}}</p>
<p>Data: {{issueDate}} Termin: {{paymentDue}}</p>
<p>Sprzedawca: {{sellerName}} NIP: {{sellerNip}}</p>
<p>{{sellerAddress}} tel. {{sellerPhone}}</p>
<p>Nabywca: {{buyerCompany}} NIP: {{buyerNip}}</p>
<p>{{buyerStreet}} {{buyerPostCode}} {{buyerCity}} {{buyerEmail}}</p>
<table><tbody>{{itemsTable}}</tbody></table>
<p>Netto: {{nettoTotal}} {{currency}}</p>
<p>VAT {{vatRate}}%: {{vatAmount}} {{currency}}</p>
<p>Brutto: {{bruttoTotal}} {{currency}}</p>
<p>Konto: {{bankAccount}}</p>
{{licenceSection}}
</body>
</html>"""
    }
}
