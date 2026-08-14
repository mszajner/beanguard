package dev.beanguard.server

import dev.beanguard.api.models.licence.LicenceCreateRequest
import dev.beanguard.api.models.shop.*
import dev.beanguard.server.controllers.OrdersController
import dev.beanguard.server.controllers.open.OpenShopController
import dev.beanguard.server.controllers.ProductsController
import dev.beanguard.server.exceptions.OpenOrdersExistException
import dev.beanguard.server.exceptions.OrderAlreadyProcessedException
import dev.beanguard.server.repositories.LicenceRepository
import dev.beanguard.server.services.LicenceService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.transaction.annotation.Transactional

import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit

@Transactional
class OrdersAcceptanceSpec extends IntegrationSpec {

    @Autowired OrdersController ordersController
    @Autowired OpenShopController openShopController
    @Autowired ProductsController productsController
    @Autowired LicenceService licenceService
    @Autowired LicenceRepository licenceRepository

    def "createOrder returns order with NEW status and snapshots product name and price"() {
        given:
        def product = productsController.createProduct(
                new CreateProductRequest("Users Pack", null, ProductType.LIMIT, "users",
                        new BigDecimal("49.99"), new BigDecimal("499.99"), true, false, null))

        when:
        def order = openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_YEAR, null, "1234567890", "new@example.com",
                null, null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 3)]))

        then:
        order.id() != null
        order.status() == OrderStatus.NEW
        order.items().size() == 1
        order.items()[0].name() == "Users Pack"
        order.items()[0].price() == new BigDecimal("499.99")
        order.items()[0].quantity() == 3
    }

    def "acceptOrder creates new licence with expected claims when licenceId is null"() {
        given:
        def product = productsController.createProduct(
                new CreateProductRequest("Feature X", null, ProductType.FEATURE, "feature_x",
                        new BigDecimal("19.99"), new BigDecimal("199.99"), true, false, null))
        def order = openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_YEAR, null, "1111111111", "newlicence@example.com",
                "Corp A", null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 1)]))

        when:
        def accepted = ordersController.acceptOrder(order.id())

        then:
        accepted.status() == OrderStatus.ACCEPTED
        accepted.licenceId() != null
        def licence = licenceRepository.findByKey(accepted.licenceId()).get()
        licence.claims["feature_x"] == "true"
        licence.email == "newlicence@example.com"
    }

    def "acceptOrder modifies existing licence claims when licenceId is set"() {
        given:
        def licence = licenceService.createLicence(LicenceCreateRequest.builder()
                .expiration(Instant.now().plus(30, ChronoUnit.DAYS))
                .vatId("2222222222")
                .email("existing@example.com")
                .claims(["users": "3"])
                .build())
        def product = productsController.createProduct(
                new CreateProductRequest("Users", null, ProductType.LIMIT, "users",
                        new BigDecimal("9.99"), new BigDecimal("99.99"), true, false, null))
        def order = openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_MONTH, licence.key, "2222222222", "existing@example.com",
                null, null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 5)]))

        when:
        ordersController.acceptOrder(order.id())

        then:
        def updated = licenceRepository.findByKey(licence.key).get()
        updated.claims["users"] == "5"
    }

    def "acceptOrder on already-accepted order returns 409"() {
        given:
        def product = productsController.createProduct(
                new CreateProductRequest("P", null, ProductType.LIMIT, "x",
                        new BigDecimal("1.00"), new BigDecimal("10.00"), true, false, null))
        def order = openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_YEAR, null, "3333333333", "double@example.com",
                null, null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 1)]))
        ordersController.acceptOrder(order.id())

        when:
        ordersController.acceptOrder(order.id())

        then:
        thrown(OrderAlreadyProcessedException)
    }

    def "createOrder throws OpenOrdersExistException when licence already has open order"() {
        given:
        def licence = licenceService.createLicence(LicenceCreateRequest.builder()
                .expiration(Instant.now().plus(30, ChronoUnit.DAYS))
                .vatId("5555555555")
                .email("open@example.com")
                .build())
        def product = productsController.createProduct(
                new CreateProductRequest("P2", null, ProductType.LIMIT, "p2",
                        new BigDecimal("1.00"), new BigDecimal("10.00"), true, false, null))
        openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_YEAR, licence.key, "5555555555", "open@example.com",
                null, null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 1)]))

        when:
        openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_YEAR, licence.key, "5555555555", "open@example.com",
                null, null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 1)]))

        then:
        def ex = thrown(OpenOrdersExistException)
        ex.openOrderIds.size() == 1
    }

    def "cancelOrder sets status to CANCELED"() {
        given:
        def product = productsController.createProduct(
                new CreateProductRequest("Q", null, ProductType.LIMIT, "y",
                        new BigDecimal("1.00"), new BigDecimal("10.00"), true, false, null))
        def order = openShopController.createOrder(new CreateOrderRequest(
                OrderPeriod.ONE_YEAR, null, "4444444444", "cancel@example.com",
                null, null, null, null, null,
                [new CreateOrderItemRequest(product.id(), 1)]))

        when:
        def canceled = ordersController.cancelOrder(order.id())

        then:
        canceled.status() == OrderStatus.CANCELED
    }
}
