package dev.beanguard.server

import dev.beanguard.api.models.shop.CreateProductRequest
import dev.beanguard.api.models.shop.ProductType
import dev.beanguard.api.models.shop.UpdateProductRequest
import dev.beanguard.server.controllers.ProductsController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

import java.math.BigDecimal

@Transactional
class ProductsAcceptanceSpec extends IntegrationSpec {

    @Autowired ProductsController productsController

    def "createProduct returns created product with correct fields"() {
        given:
        def request = new CreateProductRequest("Dodatkowi użytkownicy", "Rozszerza limit",
                ProductType.LIMIT, "users", new BigDecimal("49.99"), new BigDecimal("499.99"), true, false, null)

        when:
        def product = productsController.createProduct(request)

        then:
        product.id() != null
        product.name() == "Dodatkowi użytkownicy"
        product.type() == ProductType.LIMIT
        product.claim() == "users"
        product.priceOneMonth() == new BigDecimal("49.99")
        product.priceOneYear() == new BigDecimal("499.99")
        product.enabled()
    }

    def "getProducts returns all products including disabled"() {
        given:
        productsController.createProduct(new CreateProductRequest("A", null, ProductType.FEATURE, "feat_a",
                new BigDecimal("9.99"), new BigDecimal("99.99"), true, false, null))
        productsController.createProduct(new CreateProductRequest("B", null, ProductType.LIMIT, "b",
                new BigDecimal("19.99"), new BigDecimal("199.99"), false, false, null))

        when:
        def products = productsController.getProducts()

        then:
        products.size() >= 2
        products.any { it.enabled() }
        products.any { !it.enabled() }
    }

    def "updateProduct changes product fields"() {
        given:
        def created = productsController.createProduct(
                new CreateProductRequest("Old", null, ProductType.LIMIT, "old_claim",
                        new BigDecimal("9.99"), new BigDecimal("99.99"), true, false, null))

        when:
        def updated = productsController.updateProduct(created.id(),
                new UpdateProductRequest("New", "desc", ProductType.FEATURE, "new_claim",
                        new BigDecimal("29.99"), new BigDecimal("299.99"), false, false, null))

        then:
        updated.name() == "New"
        updated.type() == ProductType.FEATURE
        updated.claim() == "new_claim"
        !updated.enabled()
    }

    def "deleteProduct removes product"() {
        given:
        def created = productsController.createProduct(
                new CreateProductRequest("ToDelete", null, ProductType.LIMIT, "x",
                        new BigDecimal("1.00"), new BigDecimal("10.00"), true, false, null))

        when:
        productsController.deleteProduct(created.id())

        then:
        productsController.getProducts().every { it.id() != created.id() }
    }
}
