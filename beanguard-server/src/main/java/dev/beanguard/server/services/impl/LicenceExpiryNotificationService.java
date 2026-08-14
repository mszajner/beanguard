package dev.beanguard.server.services.impl;

import dev.beanguard.server.entities.LicenceEntity;
import dev.beanguard.server.models.ParameterName;
import dev.beanguard.server.repositories.LicenceRepository;
import dev.beanguard.server.services.MailService;
import dev.beanguard.server.services.ParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class LicenceExpiryNotificationService {

    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");

    private final LicenceRepository licenceRepository;
    private final MailService mailService;
    private final ParameterService parameterService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendExpiryWarnings() {
        List<Integer> thresholds = parseThresholds(
                parameterService.getString(ParameterName.MAIL_EXPIRY_WARNING_DAYS));
        Instant now = Instant.now();
        for (int days : thresholds) {
            Instant from = now.atZone(WARSAW).toLocalDate().plusDays(days)
                    .atStartOfDay(WARSAW).toInstant();
            Instant to = now.atZone(WARSAW).toLocalDate().plusDays(days)
                    .plusDays(1).atStartOfDay(WARSAW).toInstant().minusNanos(1);
            List<LicenceEntity> expiring = licenceRepository.findByExpirationBetween(from, to);
            for (LicenceEntity licence : expiring) {
                if (licence.getEmail() == null || licence.getEmail().isBlank()) continue;
                mailService.sendExpiryWarning(
                        licence.getEmail(),
                        licence.getCompanyName(),
                        licence.getExpiration(),
                        days,
                        licence.getType());
            }
        }
    }

    private List<Integer> parseThresholds(String raw) {
        try {
            return Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .toList();
        } catch (NumberFormatException e) {
            log.warn("Invalid MAIL_EXPIRY_WARNING_DAYS value '{}', using default", raw);
            return Arrays.stream(ParameterName.MAIL_EXPIRY_WARNING_DAYS.getDefaultValue().split(","))
                    .map(Integer::parseInt)
                    .toList();
        }
    }
}
