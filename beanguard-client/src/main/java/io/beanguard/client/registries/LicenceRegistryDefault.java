package io.beanguard.client.registries;

import io.beanguard.api.models.licence.Licence;
import io.beanguard.client.events.LicenceLoaded;
import io.beanguard.client.exceptions.MissingOrInvalidLicence;
import io.beanguard.client.server.BeanGuardServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor
public class LicenceRegistryDefault implements LicenceRegistry {

    private final BeanGuardServer beanGuardServer;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Getter
    private volatile LicenceStatus status = LicenceStatus.NOT_LOADED;
    @Getter
    private volatile Licence licence;

    @Override
    @Scheduled(fixedDelay = 3600000)
    @EventListener(ApplicationReadyEvent.class)
    public synchronized void refreshLicence() {
        try {
            Optional<Licence> licence = beanGuardServer.getLicence();
            if (licence.isPresent()) {
                this.licence = licence.get();
                status = Instant.now().isAfter(this.licence.getExpiration()) ? LicenceStatus.EXPIRED : LicenceStatus.LOADED;
            } else {
                this.licence = null;
                status = LicenceStatus.NOT_LOADED;
            }
        } catch (Throwable e) {
            status = (e instanceof MissingOrInvalidLicence) ? LicenceStatus.MISSING_KEY : LicenceStatus.NOT_LOADED;
            licence = null;
        }
        applicationEventPublisher.publishEvent(new LicenceLoaded(status, licence));
    }

    @Override
    public synchronized long getLimit(String name) {
        return status.isValid() ? Long.parseLong(licence.getClaims().getOrDefault(name, "0")) : 0L;
    }
}
