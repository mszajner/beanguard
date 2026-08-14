package dev.beanguard.server.services.impl;

import dev.beanguard.api.models.licence.LicenceType;
import dev.beanguard.api.models.shop.*;
import dev.beanguard.server.entities.LicenceEntity;
import dev.beanguard.server.entities.OrderEntity;
import dev.beanguard.server.entities.OrderItemEntity;
import dev.beanguard.server.entities.ProductEntity;
import dev.beanguard.server.exceptions.LicenceNotFoundException;
import dev.beanguard.server.exceptions.OpenOrdersExistException;
import dev.beanguard.server.exceptions.OrderAlreadyProcessedException;
import dev.beanguard.server.exceptions.OrderNotFoundException;
import dev.beanguard.server.exceptions.ProFormaNotFoundException;
import dev.beanguard.server.repositories.ProFormaRepository;
import dev.beanguard.server.mappers.OrderMapper;
import dev.beanguard.server.ports.InstantProvider;
import dev.beanguard.server.ports.SecureStringProvider;
import dev.beanguard.server.repositories.LicenceRepository;
import dev.beanguard.server.repositories.OrderRepository;
import dev.beanguard.server.repositories.ProductRepository;
import dev.beanguard.server.entities.ProFormaEntity;
import dev.beanguard.server.services.CertificateService;
import dev.beanguard.server.services.MailService;
import dev.beanguard.server.services.OrderService;
import dev.beanguard.server.services.ProFormaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final LicenceRepository licenceRepository;
    private final OrderMapper orderMapper;
    private final SecureStringProvider secureStringProvider;
    private final InstantProvider instantProvider;
    private final MailService mailService;
    private final ProFormaService proFormaService;
    private final ProFormaRepository proFormaRepository;
    private final CertificateService certificateService;
    private final OrderNumberAllocator orderNumberAllocator;

    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        OrderEntity order = new OrderEntity();
        order.setStatus(OrderStatus.NEW);
        order.setLicenceId(request.licenceId());
        order.setPeriod(request.period());
        order.setVatId(request.vatId());
        order.setEmail(request.email());
        order.setCompanyName(request.companyName());
        order.setStreet(request.street());
        order.setPostCode(request.postCode());
        order.setCity(request.city());
        order.setPhoneNumber(request.phoneNumber());

        LicenceEntity existingLicence = null;
        if (request.licenceId() != null) {
            existingLicence = licenceRepository.findByKey(request.licenceId())
                    .orElseThrow(() -> new LicenceNotFoundException(request.licenceId()));
            List<OrderEntity> openOrders = orderRepository.findByLicenceIdAndStatusNotIn(
                    request.licenceId(), List.of(OrderStatus.ACCEPTED, OrderStatus.CANCELED));
            if (!openOrders.isEmpty()) {
                List<String> orderNumbers = openOrders.stream()
                        .map(o -> o.getNumber() != null ? o.getNumber()
                                : o.getId().toString().substring(0, 8).toUpperCase())
                        .toList();
                throw new OpenOrdersExistException(orderNumbers);
            }
        }

        Set<UUID> orderedIds = request.items().stream()
                .map(CreateOrderItemRequest::productId)
                .collect(java.util.stream.Collectors.toSet());
        List<String> missing = productRepository.findAllByEnabledTrueOrderBySortOrderAsc().stream()
                .filter(ProductEntity::isRequired)
                .filter(p -> !orderedIds.contains(p.getId()))
                .map(ProductEntity::getName)
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Order is missing required products: " + String.join(", ", missing));
        }

        List<OrderItemEntity> items = new ArrayList<>();
        for (CreateOrderItemRequest itemRequest : request.items()) {
            ProductEntity product = productRepository.findById(itemRequest.productId())
                    .filter(ProductEntity::isEnabled)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Product not found or disabled: " + itemRequest.productId()));

            if (existingLicence != null && product.getType() == ProductType.LIMIT) {
                int currentQty = existingLicence.getClaims() != null
                        ? Integer.parseInt(existingLicence.getClaims().getOrDefault(product.getClaim(), "0"))
                        : 0;
                if (itemRequest.quantity() < currentQty) {
                    throw new IllegalArgumentException(
                            "Cannot decrease quantity of '" + product.getClaim() + "'. Current: " + currentQty);
                }
            }

            OrderItemEntity item = new OrderItemEntity();
            item.setOrder(order);
            item.setProduct(product);
            item.setName(product.getName());
            item.setPrice(resolveUnitPrice(product, request.period()));
            item.setQuantity(itemRequest.quantity());
            items.add(item);
        }
        order.setItems(items);
        order.setCreditAmount(existingLicence != null
                ? computeCredit(existingLicence, instantProvider.now())
                : BigDecimal.ZERO);

        int year = instantProvider.now().atZone(ZoneId.of("Europe/Warsaw")).getYear();
        order.setNumber(orderNumberAllocator.allocateNext(year));

        OrderEntity savedEntity = orderRepository.save(order);
        Order saved = orderMapper.toDto(savedEntity);
        if (saved.email() != null) {
            LocalDate issueDate = instantProvider.now()
                    .atZone(ZoneId.of("Europe/Warsaw")).toLocalDate();
            Instant orderCreatedAt = savedEntity.getCreatedAt() != null
                    ? savedEntity.getCreatedAt() : instantProvider.now();
            Instant expirationBase = existingLicence != null
                    && existingLicence.getType() == LicenceType.DEMO
                    && existingLicence.getExpiration() != null
                    && existingLicence.getExpiration().isAfter(orderCreatedAt)
                    ? existingLicence.getExpiration()
                    : orderCreatedAt;
            LocalDate projectedExpiry = newExpiration(savedEntity.getPeriod(), expirationBase)
                    .atZone(ZoneId.of("Europe/Warsaw")).toLocalDate();
            ProFormaEntity proForma = proFormaService.generate(
                    savedEntity, issueDate, savedEntity.getLicenceId(), projectedExpiry);
            mailService.sendOrderConfirmation(saved.email(), saved, proForma.getPdf(), proForma.getNumber());
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(UUID id) {
        Order dto = orderRepository.findById(id)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new OrderNotFoundException(id));
        String proFormaNumber = proFormaRepository.findByOrder_Id(id)
                .map(ProFormaEntity::getNumber)
                .orElse(null);
        return enrich(dto, proFormaNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getProFormaPdf(UUID orderId) {
        return proFormaRepository.findByOrder_Id(orderId)
                .map(ProFormaEntity::getPdf)
                .orElseThrow(() -> new ProFormaNotFoundException(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> getOrders(String status, String query, Pageable pageable) {
        Specification<OrderEntity> spec = (root, q, cb) -> cb.conjunction();
        if (status != null && !status.isBlank()) {
            try {
                OrderStatus statusEnum = OrderStatus.valueOf(status);
                spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), statusEnum));
            } catch (IllegalArgumentException ignored) {
                spec = spec.and((root, q, cb) -> cb.disjunction());
            }
        }
        if (query != null && !query.isBlank()) {
            String pattern = "%" + escapeLike(query.toLowerCase()) + "%";
            spec = spec.and((root, q, cb) -> {
                var sub = q.subquery(UUID.class);
                var item = sub.from(OrderItemEntity.class);
                sub.select(item.get("order").get("id"))
                   .where(cb.like(cb.lower(item.get("name")), pattern, '\\'));
                var proFormaSub = q.subquery(UUID.class);
                var proForma = proFormaSub.from(ProFormaEntity.class);
                proFormaSub.select(proForma.get("order").get("id"))
                           .where(cb.like(cb.lower(proForma.get("number")), pattern, '\\'));
                List<Predicate> predicates = new ArrayList<>(List.of(
                    cb.like(cb.lower(root.get("email")), pattern, '\\'),
                    cb.like(cb.lower(root.get("vatId")), pattern, '\\'),
                    cb.like(cb.lower(root.get("companyName")), pattern, '\\'),
                    cb.like(cb.lower(root.get("street")), pattern, '\\'),
                    cb.like(cb.lower(root.get("postCode")), pattern, '\\'),
                    cb.like(cb.lower(root.get("city")), pattern, '\\'),
                    cb.like(cb.lower(root.get("phoneNumber")), pattern, '\\'),
                    cb.like(cb.lower(root.get("number")), pattern, '\\'),
                    root.get("id").in(sub),
                    root.get("id").in(proFormaSub)
                ));
                try {
                    predicates.add(cb.equal(root.get("licenceId"), UUID.fromString(query.trim())));
                } catch (IllegalArgumentException ignored) {}
                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }
        Page<Order> page = orderRepository.findAll(spec, pageable).map(orderMapper::toDto);
        List<UUID> orderIds = page.getContent().stream().map(Order::id).toList();
        Map<UUID, String> proFormaNumbers = proFormaRepository.findByOrder_IdIn(orderIds)
                .stream()
                .collect(Collectors.toMap(pf -> pf.getOrder().getId(), ProFormaEntity::getNumber));
        return page.map(o -> enrich(o, proFormaNumbers.get(o.id())));
    }

    @Override
    @Transactional
    public Order acceptOrder(UUID id) {
        OrderEntity order = orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.NEW) {
            throw new OrderAlreadyProcessedException(id);
        }

        Map<String, String> claims = new HashMap<>();
        LicenceEntity existingLicence = null;
        if (order.getLicenceId() != null) {
            existingLicence = licenceRepository.findByKey(order.getLicenceId())
                    .orElseThrow(() -> new LicenceNotFoundException(order.getLicenceId()));
            if (existingLicence.getClaims() != null) {
                claims.putAll(existingLicence.getClaims());
            }
        }

        for (OrderItemEntity item : order.getItems()) {
            ProductEntity product = item.getProduct();
            if (product.getType() == ProductType.FEATURE) {
                claims.put(product.getClaim(), "true");
            } else {
                claims.put(product.getClaim(), String.valueOf(item.getQuantity()));
            }
        }

        BigDecimal netAmount = computeNetAmount(order);
        if (existingLicence != null) {
            existingLicence.setClaims(claims);
            Instant expirationBase = existingLicence.getType() == LicenceType.DEMO
                    && existingLicence.getExpiration() != null
                    && existingLicence.getExpiration().isAfter(order.getCreatedAt())
                    ? existingLicence.getExpiration()
                    : order.getCreatedAt();
            existingLicence.setExpiration(newExpiration(order.getPeriod(), expirationBase));
            if (existingLicence.getType() == LicenceType.DEMO) {
                existingLicence.setType(LicenceType.STANDARD);
            }
            existingLicence.setNetAmount(netAmount);
            existingLicence.setLastPeriod(order.getPeriod());
            copyBuyerData(order, existingLicence);
            licenceRepository.save(existingLicence);
            byte[] existingCert = certificateService.generate(existingLicence);
            existingLicence.setCertificate(existingCert);
            licenceRepository.save(existingLicence);
            if (existingLicence.getEmail() != null && existingLicence.getType() == LicenceType.STANDARD) {
                mailService.sendLicenceCreated(existingLicence.getEmail(), existingLicence.getKey(), existingCert);
            }
        } else {
            LicenceEntity newLicence = buildNewLicence(order, claims, newExpiration(order.getPeriod(), order.getCreatedAt()));
            newLicence.setNetAmount(netAmount);
            newLicence.setLastPeriod(order.getPeriod());
            LicenceEntity saved = licenceRepository.save(newLicence);
            order.setLicenceId(saved.getKey());
            byte[] newCert = certificateService.generate(saved);
            saved.setCertificate(newCert);
            licenceRepository.save(saved);
            if (saved.getEmail() != null) {
                mailService.sendLicenceCreated(saved.getEmail(), saved.getKey(), newCert);
            }
        }

        order.setStatus(OrderStatus.ACCEPTED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        if (order.getStatus() != OrderStatus.NEW) {
            throw new OrderAlreadyProcessedException(id);
        }
        order.setStatus(OrderStatus.CANCELED);
        Order dto = orderMapper.toDto(orderRepository.save(order));
        if (order.getEmail() != null && !order.getEmail().isBlank()) {
            mailService.sendOrderCancellation(order.getEmail(), dto);
        }
        return dto;
    }

    private Instant newExpiration(OrderPeriod period, Instant from) {
        return period == OrderPeriod.ONE_YEAR
                ? instantProvider.plusMonths(12, from)
                : instantProvider.plusMonth(from);
    }

    private static void copyBuyerData(OrderEntity order, LicenceEntity licence) {
        licence.setVatId(order.getVatId());
        licence.setEmail(order.getEmail());
        licence.setCompanyName(order.getCompanyName());
        licence.setStreet(order.getStreet());
        licence.setPostCode(order.getPostCode());
        licence.setCity(order.getCity());
        licence.setPhoneNumber(order.getPhoneNumber());
    }

    private LicenceEntity buildNewLicence(OrderEntity order, Map<String, String> claims, Instant expiration) {
        LicenceEntity licence = new LicenceEntity();
        copyBuyerData(order, licence);
        licence.setClaims(claims);
        licence.setType(LicenceType.STANDARD);
        licence.setExpiration(expiration);
        licence.setSecret(secureStringProvider.generate(80));
        return licence;
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private BigDecimal resolveUnitPrice(ProductEntity product, OrderPeriod period) {
        return switch (period) {
            case ONE_YEAR -> product.getPriceOneYear();
            case ONE_MONTH -> product.getPriceOneMonth();
        };
    }

    private BigDecimal computeNetAmount(OrderEntity order) {
        return order.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order enrich(Order dto, String proFormaNumber) {
        return new Order(dto.id(), dto.status(), dto.licenceId(), dto.period(),
                         dto.companyName(), dto.street(), dto.postCode(), dto.city(),
                         dto.vatId(), dto.email(), dto.phoneNumber(),
                         dto.items(), dto.creditAmount(), dto.createdAt(), dto.updatedAt(),
                         dto.number(), proFormaNumber);
    }

    private BigDecimal computeCredit(LicenceEntity licence, Instant now) {
        if (licence.getNetAmount() == null || licence.getLastPeriod() == null) {
            return BigDecimal.ZERO;
        }
        Instant expiration = licence.getExpiration();
        if (expiration == null || !expiration.isAfter(now)) {
            return BigDecimal.ZERO;
        }
        ZonedDateTime expZdt = expiration.atZone(ZoneOffset.UTC);
        ZonedDateTime startZdt = licence.getLastPeriod() == OrderPeriod.ONE_YEAR
                ? expZdt.minusYears(1)
                : expZdt.minusMonths(1);
        long totalDays = ChronoUnit.DAYS.between(startZdt.toInstant(), expiration);
        long remainingDays = Math.min(totalDays, Math.max(0L, ChronoUnit.DAYS.between(now, expiration)));
        if (totalDays <= 0) return BigDecimal.ZERO;
        return licence.getNetAmount()
                .multiply(BigDecimal.valueOf(remainingDays))
                .divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP);
    }
}
