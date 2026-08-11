package io.beanguard.server

import io.beanguard.api.models.shop.*
import io.beanguard.server.controllers.ProductsController
import io.beanguard.server.controllers.open.OpenShopController
import io.beanguard.server.repositories.ProFormaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

import java.math.BigDecimal

@Transactional
class ProFormaAcceptanceSpec extends IntegrationSpec {

    @Autowired OpenShopController openShopController
    @Autowired ProductsController productsController
    @Autowired ProFormaRepository proFormaRepository

    def "createOrder generates and persists a pro-forma PDF with correct number format"() {
        given:
        def product = productsController.createProduct(
                new CreateProductRequest("BeanGuard Standard", null, ProductType.LIMIT, "users",
                        new BigDecimal("49.99"), new BigDecimal("499.99"), true, false, null))

        when:
        def order = openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_YEAR, null, "1234567890", "buyer@example.com",
                "Firma Testowa Sp. z o.o.", "ul. Testowa 1", "00-001", "Warszawa", null,
                [new CreateOrderItemRequest(product.id(), 2)]))

        then:
        def proForma = proFormaRepository.findByOrder_Id(order.id())
        proForma.isPresent()
        proForma.get().number ==~ /PF\/\d{4}\/\d{3}/
        proForma.get().pdf != null
        proForma.get().pdf.length > 0
    }

    def "sequential orders get incrementing pro-forma numbers within the same year"() {
        given:
        def product = productsController.createProduct(
                new CreateProductRequest("Module X", null, ProductType.FEATURE, "module_x",
                        new BigDecimal("10.00"), new BigDecimal("100.00"), true, false, null))

        when:
        def order1 = openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_MONTH, null, "1111111111", "a@example.com",
                null, null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 1)]))
        def order2 = openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_MONTH, null, "2222222222", "b@example.com",
                null, null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 1)]))

        then:
        def pf1 = proFormaRepository.findByOrder_Id(order1.id()).get()
        def pf2 = proFormaRepository.findByOrder_Id(order2.id()).get()
        pf1.number != pf2.number
        pf1.number ==~ /PF\/\d{4}\/\d{3}/
        pf2.number ==~ /PF\/\d{4}\/\d{3}/
        // numbers differ by exactly 1
        def n1 = pf1.number.split("/")[2] as int
        def n2 = pf2.number.split("/")[2] as int
        n2 == n1 + 1
    }
}
