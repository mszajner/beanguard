package io.beanguard.client.events;

import io.beanguard.api.models.licence.Licence;
import io.beanguard.client.registries.LicenceStatus;
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
