package io.beanguard.server

import io.beanguard.api.models.licence.LicenceType
import io.beanguard.api.models.shop.*
import io.beanguard.server.entities.LicenceEntity
import io.beanguard.server.entities.OrderEntity
import io.beanguard.server.entities.OrderItemEntity
import io.beanguard.server.entities.ProductEntity
import io.beanguard.server.exceptions.OrderAlreadyProcessedException
import io.beanguard.server.exceptions.ProFormaNotFoundException
import io.beanguard.server.mappers.OrderMapper
import io.beanguard.server.ports.InstantProvider
import io.beanguard.server.ports.SecureStringProvider
import io.beanguard.server.repositories.LicenceRepository
import io.beanguard.server.repositories.OrderRepository
import io.beanguard.server.repositories.ProductRepository
import io.beanguard.server.repositories.ProFormaRepository
import io.beanguard.server.entities.ProFormaEntity
import io.beanguard.server.services.CertificateService
import io.beanguard.server.services.MailService
import io.beanguard.server.services.ProFormaService
import io.beanguard.server.services.impl.OrderNumberAllocator
import io.beanguard.server.services.impl.OrderServiceImpl
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import spock.lang.Specification

import java.math.BigDecimal
import java.time.Instant

class OrderServiceTest extends Specification {

    OrderRepository orderRepository = Mock()
    ProductRepository productRepository = Mock()
    LicenceRepository licenceRepository = Mock()
    OrderMapper orderMapper = Mock()
    SecureStringProvider secureStringProvider = Mock()
    InstantProvider instantProvider = Mock()
    MailService mailService = Mock()
    ProFormaService proFormaService = Mock()
    ProFormaRepository proFormaRepository = Mock()
    CertificateService certificateService = Mock()
    OrderNumberAllocator orderNumberAllocator = Mock()
    OrderServiceImpl service

    def setup() {
        service = new OrderServiceImpl(orderRepository, productRepository, licenceRepository,
                orderMapper, secureStringProvider, instantProvider, mailService, proFormaService, proFormaRepository, certificateService, orderNumberAllocator)
        orderNumberAllocator.allocateNext(_) >> "ZAM/2026/001"
        instantProvider.now() >> Instant.now()
        certificateService.generate(_) >> new byte[0]
        secureStringProvider.generate(80) >> "secret"
        productRepository.findAllByEnabledTrueOrderBySortOrderAsc() >> []
        def mockProForma = new ProFormaEntity()
        mockProForma.pdf = new byte[0]
        mockProForma.number = "PF/2026/001"
        proFormaService.generate(_, _, _, _) >> mockProForma
    }

    def "acceptOrder creates new STANDARD licence with LIMIT claim set to quantity"() {
        given:
        def product = product(ProductType.LIMIT, "users")
        def item = item(product, 5)
        def order = order(OrderStatus.NEW, null, OrderPeriod.ONE_YEAR, [item])
        def savedOrder = new OrderEntity()
        instantProvider.now() >> Instant.now()
        instantProvider.plusMonths(12, _) >> Instant.now().plusSeconds(31536000)
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)
        orderRepository.save(_) >> { OrderEntity o -> savedOrder }
        orderMapper.toDto(_) >> Mock(Order)
        LicenceEntity savedLicence = null
        licenceRepository.save(_) >> { LicenceEntity l -> savedLicence = l; l.key = UUID.randomUUID(); l }

        when:
        service.acceptOrder(order.id)

        then:
        savedLicence != null
        savedLicence.claims["users"] == "5"
        savedLicence.type == LicenceType.STANDARD
    }

    def "acceptOrder creates new licence with FEATURE claim set to true"() {
        given:
        def product = product(ProductType.FEATURE, "feature_x")
        def item = item(product, 1)
        def order = order(OrderStatus.NEW, null, OrderPeriod.ONE_MONTH, [item])
        instantProvider.now() >> Instant.now()
        instantProvider.plusMonth(_) >> Instant.now().plusSeconds(2592000)
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)
        orderRepository.save(_) >> order
        orderMapper.toDto(_) >> Mock(Order)
        LicenceEntity savedLicence = null
        licenceRepository.save(_) >> { LicenceEntity l -> savedLicence = l; l.key = UUID.randomUUID(); l }

        when:
        service.acceptOrder(order.id)

        then:
        savedLicence.claims["feature_x"] == "true"
    }

    def "acceptOrder creates new licence with expiration now+1year for ONE_YEAR period"() {
        given:
        def expectedExpiration = Instant.now().plusSeconds(31536000)
        def product = product(ProductType.LIMIT, "users")
        def item = item(product, 1)
        def order = order(OrderStatus.NEW, null, OrderPeriod.ONE_YEAR, [item])
        instantProvider.now() >> Instant.now()
        instantProvider.plusMonths(12, _) >> expectedExpiration
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)
        orderRepository.save(_) >> order
        orderMapper.toDto(_) >> Mock(Order)
        LicenceEntity savedLicence = null
        licenceRepository.save(_) >> { LicenceEntity l -> savedLicence = l; l.key = UUID.randomUUID(); l }

        when:
        service.acceptOrder(order.id)

        then:
        savedLicence.expiration == expectedExpiration
    }

    def "acceptOrder creates new licence with expiration now+1month for ONE_MONTH period"() {
        given:
        def expectedExpiration = Instant.now().plusSeconds(2592000)
        def product = product(ProductType.LIMIT, "users")
        def item = item(product, 1)
        def order = order(OrderStatus.NEW, null, OrderPeriod.ONE_MONTH, [item])
        instantProvider.now() >> Instant.now()
        instantProvider.plusMonth(_) >> expectedExpiration
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)
        orderRepository.save(_) >> order
        orderMapper.toDto(_) >> Mock(Order)
        LicenceEntity savedLicence = null
        licenceRepository.save(_) >> { LicenceEntity l -> savedLicence = l; l.key = UUID.randomUUID(); l }

        when:
        service.acceptOrder(order.id)

        then:
        savedLicence.expiration == expectedExpiration
    }

    def "acceptOrder throws OrderAlreadyProcessedException when status is ACCEPTED"() {
        given:
        def order = order(OrderStatus.ACCEPTED, null, OrderPeriod.ONE_YEAR, [])
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)

        when:
        service.acceptOrder(order.id)

        then:
        thrown(OrderAlreadyProcessedException)
    }

    def "acceptOrder throws OrderAlreadyProcessedException when status is CANCELED"() {
        given:
        def order = order(OrderStatus.CANCELED, null, OrderPeriod.ONE_YEAR, [])
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)

        when:
        service.acceptOrder(order.id)

        then:
        thrown(OrderAlreadyProcessedException)
    }

    def "createOrder snapshots priceOneYear when period is ONE_YEAR"() {
        given:
        def product = product(ProductType.LIMIT, "users")
        productRepository.findById(product.id) >> Optional.of(product)
        orderRepository.save(_) >> { OrderEntity o -> o.id = UUID.randomUUID(); o }
        orderMapper.toDto(_) >> Mock(Order)
        def request = new CreateOrderRequest(OrderPeriod.ONE_YEAR, null, "1234567890",
                "test@example.com", null, null, null, null, null,
                [new CreateOrderItemRequest(product.id, 2)])

        when:
        service.createOrder(request)

        then:
        1 * orderRepository.save({ OrderEntity o ->
            o.items[0].price == new BigDecimal("99.99")
        })
    }

    def "createOrder snapshots priceOneMonth when period is ONE_MONTH"() {
        given:
        def product = product(ProductType.LIMIT, "users")
        productRepository.findById(product.id) >> Optional.of(product)
        orderRepository.save(_) >> { OrderEntity o -> o.id = UUID.randomUUID(); o }
        orderMapper.toDto(_) >> Mock(Order)
        def request = new CreateOrderRequest(OrderPeriod.ONE_MONTH, null, "1234567890",
                "test@example.com", null, null, null, null, null,
                [new CreateOrderItemRequest(product.id, 1)])

        when:
        service.createOrder(request)

        then:
        1 * orderRepository.save({ OrderEntity o ->
            o.items[0].price == new BigDecimal("9.99")
        })
    }

    def "acceptOrder sends licence-created email when new licence has email"() {
        given:
        def email = "customer@example.com"
        def licenceKey = UUID.randomUUID()
        def product = product(ProductType.LIMIT, "users")
        def item = item(product, 1)
        def order = order(OrderStatus.NEW, null, OrderPeriod.ONE_YEAR, [item])
        order.email = email
        instantProvider.now() >> Instant.now()
        instantProvider.plusMonths(12, _) >> Instant.now().plusSeconds(31536000)
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)
        orderRepository.save(_) >> { OrderEntity o -> o }
        orderMapper.toDto(_) >> Mock(Order)
        licenceRepository.save(_) >> { LicenceEntity l -> l.key = licenceKey; l }

        when:
        service.acceptOrder(order.id)

        then:
        1 * mailService.sendLicenceCreated(email, licenceKey)
    }

    def "acceptOrder extends DEMO licence from end of DEMO period and upgrades type to STANDARD"() {
        given:
        def licenceId = UUID.randomUUID()
        def demoExpiration = Instant.now().plusSeconds(86400 * 10)   // 10 days in future
        def orderCreatedAt = Instant.now()
        def expectedExpiration = demoExpiration.plusSeconds(31536000) // demoExpiration + ~1yr (mocked)
        def existingLicence = new LicenceEntity()
        existingLicence.key = licenceId
        existingLicence.type = LicenceType.DEMO
        existingLicence.expiration = demoExpiration
        existingLicence.claims = [:]
        def product = product(ProductType.LIMIT, "users")
        def item = item(product, 1)
        def order = order(OrderStatus.NEW, licenceId, OrderPeriod.ONE_YEAR, [item])
        order.createdAt = orderCreatedAt
        licenceRepository.findByKey(licenceId) >> Optional.of(existingLicence)
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)
        orderRepository.save(_) >> order
        orderMapper.toDto(_) >> Mock(Order)
        LicenceEntity savedLicence = null
        licenceRepository.save(_) >> { LicenceEntity l -> savedLicence = l; l }
        instantProvider.plusMonths(12, demoExpiration) >> expectedExpiration

        when:
        service.acceptOrder(order.id)

        then:
        savedLicence.expiration == expectedExpiration
        savedLicence.type == LicenceType.STANDARD
    }

    def "acceptOrder extends expired DEMO licence from order date and upgrades type to STANDARD"() {
        given:
        def licenceId = UUID.randomUUID()
        def demoExpiration = Instant.now().minusSeconds(86400)        // expired yesterday
        def orderCreatedAt = Instant.now()
        def expectedExpiration = orderCreatedAt.plusSeconds(31536000) // orderCreatedAt + ~1yr (mocked)
        def existingLicence = new LicenceEntity()
        existingLicence.key = licenceId
        existingLicence.type = LicenceType.DEMO
        existingLicence.expiration = demoExpiration
        existingLicence.claims = [:]
        def product = product(ProductType.LIMIT, "users")
        def item = item(product, 1)
        def order = order(OrderStatus.NEW, licenceId, OrderPeriod.ONE_YEAR, [item])
        order.createdAt = orderCreatedAt
        licenceRepository.findByKey(licenceId) >> Optional.of(existingLicence)
        orderRepository.findByIdForUpdate(order.id) >> Optional.of(order)
        orderRepository.save(_) >> order
        orderMapper.toDto(_) >> Mock(Order)
        LicenceEntity savedLicence = null
        licenceRepository.save(_) >> { LicenceEntity l -> savedLicence = l; l }
        instantProvider.plusMonths(12, orderCreatedAt) >> expectedExpiration

        when:
        service.acceptOrder(order.id)

        then:
        savedLicence.expiration == expectedExpiration
        savedLicence.type == LicenceType.STANDARD
    }

    def "getOrder returns proFormaNumber when pro-forma exists"() {
        given:
        def orderId = UUID.randomUUID()
        def entity = new OrderEntity()
        entity.id = orderId
        def mappedOrder = Mock(Order)
        mappedOrder.id() >> orderId
        def proForma = new ProFormaEntity()
        proForma.number = "PF/2026/001"
        orderRepository.findById(orderId) >> Optional.of(entity)
        orderMapper.toDto(entity) >> mappedOrder
        proFormaRepository.findByOrder_Id(orderId) >> Optional.of(proForma)

        when:
        def result = service.getOrder(orderId)

        then:
        result.proFormaNumber() == "PF/2026/001"
    }

    def "getOrder returns null proFormaNumber when no pro-forma"() {
        given:
        def orderId = UUID.randomUUID()
        def entity = new OrderEntity()
        entity.id = orderId
        def mappedOrder = Mock(Order)
        mappedOrder.id() >> orderId
        orderRepository.findById(orderId) >> Optional.of(entity)
        orderMapper.toDto(entity) >> mappedOrder
        proFormaRepository.findByOrder_Id(orderId) >> Optional.empty()

        when:
        def result = service.getOrder(orderId)

        then:
        result.proFormaNumber() == null
    }

    def "getProFormaPdf returns PDF bytes when pro-forma exists"() {
        given:
        def orderId = UUID.randomUUID()
        def pdfBytes = [1, 2, 3] as byte[]
        def proForma = new ProFormaEntity()
        proForma.pdf = pdfBytes
        proFormaRepository.findByOrder_Id(orderId) >> Optional.of(proForma)

        when:
        def result = service.getProFormaPdf(orderId)

        then:
        result == pdfBytes
    }

    def "getProFormaPdf throws ProFormaNotFoundException when no pro-forma"() {
        given:
        def orderId = UUID.randomUUID()
        proFormaRepository.findByOrder_Id(orderId) >> Optional.empty()

        when:
        service.getProFormaPdf(orderId)

        then:
        thrown(ProFormaNotFoundException)
    }

    def "getOrders enriches orders with pro-forma numbers in batch"() {
        given:
        def orderId = UUID.randomUUID()
        def entity = new OrderEntity()
        entity.id = orderId
        def mappedOrder = Mock(Order)
        mappedOrder.id() >> orderId
        def orderForProForma = new OrderEntity()
        orderForProForma.id = orderId
        def proForma = new ProFormaEntity()
        proForma.number = "PF/2026/001"
        proForma.order = orderForProForma
        orderRepository.findAll(_, _) >> new PageImpl<>([entity])
        orderMapper.toDto(entity) >> mappedOrder
        proFormaRepository.findByOrder_IdIn(_) >> [proForma]

        when:
        def result = service.getOrders(null, null, Pageable.ofSize(20))

        then:
        result.content[0].proFormaNumber() == "PF/2026/001"
    }

    // --- helpers ---

    private ProductEntity product(ProductType type, String claim) {
        def p = new ProductEntity()
        p.id = UUID.randomUUID()
        p.type = type
        p.claim = claim
        p.enabled = true
        p.name = "Product"
        p.priceOneMonth = new BigDecimal("9.99")
        p.priceOneYear = new BigDecimal("99.99")
        p
    }

    private OrderItemEntity item(ProductEntity product, int quantity) {
        def i = new OrderItemEntity()
        i.product = product
        i.name = product.name
        i.price = product.priceOneMonth
        i.quantity = quantity
        i
    }

    private OrderEntity order(OrderStatus status, UUID licenceId, OrderPeriod period,
                              List<OrderItemEntity> items) {
        def o = new OrderEntity()
        o.id = UUID.randomUUID()
        o.status = status
        o.licenceId = licenceId
        o.period = period
        o.vatId = "1234567890"
        o.email = "test@example.com"
        o.items = items
        o
    }
}
