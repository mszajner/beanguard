package io.beanguard.server.services.impl;

import io.beanguard.api.models.licence.LicenceTransferInitResponse;
import io.beanguard.api.models.licence.LicenceTransferStatusResponse;
import io.beanguard.server.entities.LicenceEntity;
import io.beanguard.server.entities.LicenceTransferEntity;
import io.beanguard.server.exceptions.LicenceNotFoundException;
import io.beanguard.server.exceptions.LicenceTransferExpiredException;
import io.beanguard.server.exceptions.LicenceTransferInitException;
import io.beanguard.server.exceptions.LicenceTransferNotFoundException;
import io.beanguard.server.ports.InstantProvider;
import io.beanguard.server.ports.SecureStringProvider;
import io.beanguard.server.repositories.LicenceRepository;
import io.beanguard.server.repositories.LicenceTransferRepository;
import io.beanguard.server.services.LicenceTransferService;
import io.beanguard.server.services.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LicenceTransferServiceImpl implements LicenceTransferService {

    private static final int TOKEN_TTL_HOURS = 1;

    private final LicenceTransferRepository licenceTransferRepository;
    private final LicenceRepository licenceRepository;
    private final MailService mailService;
    private final SecureStringProvider secureStringProvider;
    private final InstantProvider instantProvider;

    @Value("${beanguard.shop.url}")
    private String shopBaseUrl;

    @Override
    @Transactional
    public LicenceTransferInitResponse initiateTransfer(UUID licenceKey) {
        LicenceEntity licence = licenceRepository.findByKey(licenceKey)
                .orElseThrow(() -> new LicenceNotFoundException(licenceKey));
        if (licence.getEmail() == null || licence.getEmail().isBlank()) {
            throw new LicenceTransferInitException("Licence has no email address — cannot send transfer confirmation");
        }
        licenceTransferRepository.deleteByLicenceKeyAndStatus(licenceKey, "PENDING");
        Instant now = instantProvider.now();
        Instant expiresAt = now.plus(TOKEN_TTL_HOURS, ChronoUnit.HOURS);
        UUID token = UUID.randomUUID();
        licenceTransferRepository.save(new LicenceTransferEntity(token, licenceKey, "PENDING", expiresAt, now));
        String confirmUrl = shopBaseUrl + "/transfer-confirm?token=" + token;
        mailService.sendTransferConfirmation(licence.getEmail(), confirmUrl);
        return new LicenceTransferInitResponse(token, expiresAt);
    }

    @Override
    public LicenceTransferStatusResponse getStatus(UUID token) {
        LicenceTransferEntity transfer = licenceTransferRepository.findById(token)
                .orElseThrow(() -> new LicenceTransferNotFoundException(token));
        if (transfer.getExpiresAt().isBefore(instantProvider.now())) {
            licenceTransferRepository.deleteById(token);
            return new LicenceTransferStatusResponse("EXPIRED", null);
        }
        if ("PENDING".equals(transfer.getStatus())) {
            return new LicenceTransferStatusResponse("PENDING", null);
        }
        LicenceEntity licence = licenceRepository.findByKey(transfer.getLicenceKey())
                .orElseThrow(() -> new LicenceNotFoundException(transfer.getLicenceKey()));
        return new LicenceTransferStatusResponse("CONFIRMED", licence.getSecret());
    }

    @Override
    @Transactional
    public String confirm(UUID token) {
        LicenceTransferEntity transfer = licenceTransferRepository.findById(token)
                .orElseThrow(() -> new LicenceTransferNotFoundException(token));
        if (transfer.getExpiresAt().isBefore(instantProvider.now()) || "CONFIRMED".equals(transfer.getStatus())) {
            throw new LicenceTransferExpiredException(token);
        }
        String newSecret = secureStringProvider.generate(80);
        LicenceEntity licence = licenceRepository.findByKey(transfer.getLicenceKey())
                .orElseThrow(() -> new LicenceNotFoundException(transfer.getLicenceKey()));
        licence.setSecret(newSecret);
        licenceRepository.save(licence);
        transfer.setStatus("CONFIRMED");
        licenceTransferRepository.save(transfer);
        return newSecret;
    }
}
