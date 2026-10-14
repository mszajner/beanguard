package dev.beanguard.server.services.impl;

import dev.beanguard.api.models.licence.LicenceTransferInitResponse;
import dev.beanguard.api.models.licence.LicenceTransferStatusResponse;
import dev.beanguard.server.entities.LicenceEntity;
import dev.beanguard.server.entities.LicenceTransferEntity;
import dev.beanguard.server.exceptions.LicenceNotFoundException;
import dev.beanguard.server.exceptions.LicenceTransferExpiredException;
import dev.beanguard.server.exceptions.LicenceTransferNotFoundException;
import dev.beanguard.server.models.ParameterName;
import dev.beanguard.server.ports.InstantProvider;
import dev.beanguard.server.ports.SecureStringProvider;
import dev.beanguard.server.repositories.LicenceRepository;
import dev.beanguard.server.repositories.LicenceTransferRepository;
import dev.beanguard.server.services.LicenceTransferService;
import dev.beanguard.server.services.MailService;
import dev.beanguard.server.services.ParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicenceTransferServiceImpl implements LicenceTransferService {

    private static final int TOKEN_TTL_HOURS = 1;
    private static final String PENDING = "PENDING";
    private static final String CONFIRMED = "CONFIRMED";

    private final LicenceTransferRepository licenceTransferRepository;
    private final LicenceRepository licenceRepository;
    private final MailService mailService;
    private final SecureStringProvider secureStringProvider;
    private final InstantProvider instantProvider;
    private final ParameterService parameterService;

    @Value("${beanguard.shop.url}")
    private String shopBaseUrl;

    @Override
    @Transactional
    public LicenceTransferInitResponse initiateTransfer(UUID licenceKey) {
        Instant now = instantProvider.now();
        Instant expiresAt = now.plus(TOKEN_TTL_HOURS, ChronoUnit.HOURS);
        UUID token = UUID.randomUUID();
        LicenceTransferInitResponse response = new LicenceTransferInitResponse(token, expiresAt);

        Optional<LicenceEntity> licence = licenceRepository.findByKey(licenceKey);
        if (licence.isEmpty()) {
            log.info("Transfer ignored: unknown licence key");
            return response;
        }
        if (licence.get().getEmail() == null || licence.get().getEmail().isBlank()) {
            log.info("Transfer ignored: licence {} has no email address", licenceKey);
            return response;
        }
        if (licenceTransferRepository.existsByLicenceKeyAndStatusAndCreatedAtAfter(
                licenceKey, PENDING, now.minusSeconds(cooldownSeconds()))) {
            log.info("Transfer ignored: licence {} was asked to transfer within the cooldown", licenceKey);
            return response;
        }

        licenceTransferRepository.deleteByLicenceKeyAndStatus(licenceKey, PENDING);
        UUID confirmToken = UUID.randomUUID();
        licenceTransferRepository.save(
                new LicenceTransferEntity(token, licenceKey, confirmToken, PENDING, expiresAt, now));
        mailService.sendTransferConfirmation(licence.get().getEmail(),
                shopBaseUrl + "/transfer-confirm?token=" + confirmToken);
        return response;
    }

    @Override
    @Transactional
    public LicenceTransferStatusResponse getStatus(UUID token) {
        LicenceTransferEntity transfer = licenceTransferRepository.findByTokenForUpdate(token)
                .orElseThrow(() -> new LicenceTransferNotFoundException(token));
        if (transfer.getExpiresAt().isBefore(instantProvider.now())) {
            licenceTransferRepository.delete(transfer);
            return new LicenceTransferStatusResponse("EXPIRED", null);
        }
        if (PENDING.equals(transfer.getStatus())) {
            return new LicenceTransferStatusResponse(PENDING, null);
        }
        LicenceEntity licence = licenceRepository.findByKey(transfer.getLicenceKey())
                .orElseThrow(() -> new LicenceNotFoundException(transfer.getLicenceKey()));
        String newSecret = secureStringProvider.generate(80);
        licence.setSecret(newSecret);
        licenceRepository.save(licence);
        licenceTransferRepository.delete(transfer);
        return new LicenceTransferStatusResponse(CONFIRMED, newSecret);
    }

    @Override
    @Transactional
    public void confirm(UUID confirmToken) {
        LicenceTransferEntity transfer = licenceTransferRepository.findByConfirmTokenForUpdate(confirmToken)
                .orElseThrow(() -> new LicenceTransferNotFoundException(confirmToken));
        if (transfer.getExpiresAt().isBefore(instantProvider.now()) || CONFIRMED.equals(transfer.getStatus())) {
            throw new LicenceTransferExpiredException(confirmToken);
        }
        transfer.setStatus(CONFIRMED);
        licenceTransferRepository.save(transfer);
    }

    private long cooldownSeconds() {
        try {
            return Math.max(0, Long.parseLong(parameterService.getString(ParameterName.TRANSFER_INIT_COOLDOWN_SECONDS)));
        } catch (NumberFormatException e) {
            return Long.parseLong(ParameterName.TRANSFER_INIT_COOLDOWN_SECONDS.getDefaultValue());
        }
    }
}
