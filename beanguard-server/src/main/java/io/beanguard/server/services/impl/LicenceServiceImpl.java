package io.beanguard.server.services.impl;

import io.beanguard.api.models.licence.Licence;
import io.beanguard.api.models.licence.LicenceCreateRequest;
import io.beanguard.api.models.licence.LicenceDemoCreateRequest;
import io.beanguard.api.models.licence.LicenceType;
import io.beanguard.api.models.licence.LicenceUpdateRequest;
import io.beanguard.api.models.shop.ShopLicenceContext;
import io.beanguard.server.entities.LicenceEntity;
import io.beanguard.server.exceptions.DemoLicenceAlreadyExistsException;
import io.beanguard.server.exceptions.LicenceNotFoundException;
import io.beanguard.server.mappers.LicenceMapper;
import io.beanguard.server.models.ParameterName;
import io.beanguard.server.ports.InstantProvider;
import io.beanguard.server.ports.SecureStringProvider;
import io.beanguard.server.repositories.LicenceRepository;
import io.beanguard.server.repositories.ProductRepository;
import io.beanguard.server.services.CertificateService;
import io.beanguard.server.services.LicenceService;
import io.beanguard.server.services.MailService;
import io.beanguard.server.services.ParameterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenceServiceImpl implements LicenceService {

    private final LicenceRepository licenceRepository;
    private final ProductRepository productRepository;
    private final LicenceMapper licenceMapper;
    private final LicenceEncryptor licenceEncryptor;
    private final SecureStringProvider secureStringProvider;
    private final InstantProvider instantProvider;
    private final ParameterService parameterService;
    private final MailService mailService;
    private final CertificateService certificateService;

    @Override
    public String getLicence(UUID key, String secret) {
        Licence licence = licenceRepository.findByKeyAndSecret(key, secret)
                .map(licenceMapper::toDto)
                .orElseThrow(() -> new LicenceNotFoundException(key));
        return licenceEncryptor.encryptLicence(licence);
    }

    @Override
    @Transactional
    public String createDemoLicence(LicenceDemoCreateRequest req) {
        if (licenceRepository.existsByEmailAndType(req.getEmail(), LicenceType.DEMO)) {
            throw new DemoLicenceAlreadyExistsException(req.getEmail());
        }
        Map<String, String> demoClaims = parameterService.getObject(ParameterName.LICENCE_DEMO_CLAIMS, new TypeReference<>() {});
        int days;
        try {
            days = Integer.parseInt(parameterService.getString(ParameterName.LICENCE_DEMO_EXPIRATION_DAYS));
        } catch (NumberFormatException e) {
            days = Integer.parseInt(ParameterName.LICENCE_DEMO_EXPIRATION_DAYS.getDefaultValue());
        }
        Licence createdLicence = createLicence(LicenceCreateRequest.builder()
                .expiration(instantProvider.plusDays(days))
                .vatId(req.getVatId())
                .email(req.getEmail())
                .claims(demoClaims)
                .type(LicenceType.DEMO)
                .build());
        String token = licenceEncryptor.encryptLicence(createdLicence);
        mailService.sendDemoWelcome(req.getEmail(), createdLicence.getKey().toString(), createdLicence.getExpiration());
        return token;
    }

    @Override
    public Licence createLicence(LicenceCreateRequest licenceCreateRequest) {
        LicenceEntity licenceEntity = licenceMapper.toEntity(licenceCreateRequest);
        licenceEntity.setSecret(secureStringProvider.generate(80));
        LicenceEntity savedLicenceEntity = licenceRepository.save(licenceEntity);
        if (savedLicenceEntity.getEmail() != null && savedLicenceEntity.getType() != LicenceType.DEMO) {
            mailService.sendLicenceCreated(savedLicenceEntity.getEmail(), savedLicenceEntity.getKey());
        }
        if (savedLicenceEntity.getType() == LicenceType.STANDARD) {
            savedLicenceEntity.setCertificate(certificateService.generate(savedLicenceEntity));
            licenceRepository.save(savedLicenceEntity);
        }
        return licenceMapper.toDto(savedLicenceEntity);
    }

    @Override
    public Licence updateLicence(UUID key, LicenceUpdateRequest licence) {
        LicenceEntity licenceEntity = licenceRepository.findByKey(key)
                .orElseThrow(() -> new LicenceNotFoundException(key));
        licenceEntity.setExpiration(licence.getExpiration());
        licenceEntity.setCompanyName(licence.getCompanyName());
        licenceEntity.setStreet(licence.getStreet());
        licenceEntity.setPostCode(licence.getPostCode());
        licenceEntity.setCity(licence.getCity());
        licenceEntity.setVatId(licence.getVatId());
        licenceEntity.setEmail(licence.getEmail());
        licenceEntity.setPhoneNumber(licence.getPhoneNumber());
        licenceEntity.setClaims(licence.getClaims());
        LicenceEntity savedLicenceEntity = licenceRepository.save(licenceEntity);
        if (savedLicenceEntity.getType() == LicenceType.STANDARD) {
            savedLicenceEntity.setCertificate(certificateService.generate(savedLicenceEntity));
            licenceRepository.save(savedLicenceEntity);
        }
        return licenceMapper.withoutSecret(savedLicenceEntity);
    }

    @Override
    public Instant getLicenceIat(UUID key) {
        Licence licence = licenceRepository.findByKey(key)
                .map(licenceMapper::withoutSecret)
                .orElseThrow(() -> new LicenceNotFoundException(key));
        return licence.getUpdatedAt();
    }

    @Override
    public Licence getLicence(UUID key) {
        return licenceRepository.findByKey(key)
                .map(licenceMapper::withoutSecret)
                .orElseThrow(() -> new LicenceNotFoundException(key));
    }

    @Override
    public ShopLicenceContext getShopContext(UUID key) {
        LicenceEntity licence = licenceRepository.findByKey(key)
                .orElseThrow(() -> new LicenceNotFoundException(key));
        ShopLicenceContext ctx = licenceMapper.toShopContext(licence);
        Map<String, String> claims = licence.getClaims() != null ? licence.getClaims() : Map.of();
        var allProducts = productRepository.findAllByOrderBySortOrderAsc();
        Map<String, Integer> limitClaims = allProducts.stream()
                .filter(p -> p.getType() == io.beanguard.api.models.shop.ProductType.LIMIT)
                .filter(p -> claims.containsKey(p.getClaim()))
                .collect(java.util.stream.Collectors.toMap(
                        io.beanguard.server.entities.ProductEntity::getClaim,
                        p -> Integer.parseInt(claims.getOrDefault(p.getClaim(), "0"))));
        java.util.Set<String> featureClaims = allProducts.stream()
                .filter(p -> p.getType() == io.beanguard.api.models.shop.ProductType.FEATURE)
                .filter(p -> "true".equals(claims.get(p.getClaim())))
                .map(io.beanguard.server.entities.ProductEntity::getClaim)
                .collect(java.util.stream.Collectors.toSet());
        ctx.setLimitClaims(limitClaims);
        ctx.setFeatureClaims(featureClaims);
        return ctx;
    }

    @Override
    public byte[] getCertificate(UUID key) {
        LicenceEntity licence = licenceRepository.findByKey(key)
                .orElseThrow(() -> new LicenceNotFoundException(key));
        if (licence.getCertificate() == null) {
            throw new LicenceNotFoundException(key);
        }
        return licence.getCertificate();
    }

    @Override
    public Page<Licence> getLicences(String query, LicenceType type, String expirationStatus, Pageable pageable) {
        Specification<LicenceEntity> spec = (root, q, cb) -> cb.conjunction();
        if (query != null && !query.isBlank()) {
            String pattern = "%" + escapeLike(query.toLowerCase()) + "%";
            spec = spec.and((root, q, cb) -> {
                List<Predicate> predicates = new ArrayList<>(List.of(
                    cb.like(cb.lower(root.get("email")), pattern, '\\'),
                    cb.like(cb.lower(root.get("vatId")), pattern, '\\'),
                    cb.like(cb.lower(root.get("companyName")), pattern, '\\'),
                    cb.like(cb.lower(root.get("street")), pattern, '\\'),
                    cb.like(cb.lower(root.get("postCode")), pattern, '\\'),
                    cb.like(cb.lower(root.get("city")), pattern, '\\'),
                    cb.like(cb.lower(root.get("phoneNumber")), pattern, '\\')
                ));
                try {
                    predicates.add(cb.equal(root.get("key"), UUID.fromString(query.trim())));
                } catch (IllegalArgumentException ignored) {}
                return cb.or(predicates.toArray(new Predicate[0]));
            });
        }
        if (type != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("type"), type));
        }
        if ("ACTIVE".equals(expirationStatus)) {
            Instant now = instantProvider.now();
            spec = spec.and((root, q, cb) -> cb.greaterThan(root.get("expiration"), now));
        } else if ("EXPIRED".equals(expirationStatus)) {
            Instant now = instantProvider.now();
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("expiration"), now));
        }
        return licenceRepository.findAll(spec, pageable)
                .map(licenceMapper::withoutSecret);
    }

    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
