package dev.beanguard.client.events;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.client.registries.LicenceStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

public class LicenceLoaded extends ApplicationEvent {
    @Getter
    private final LicenceStatus status;
    @Getter
    private final Licence licence;

    public LicenceLoaded(LicenceStatus status, Licence licence) {
        super(status);
        this.status = status;
        this.licence = licence;
    }
}
