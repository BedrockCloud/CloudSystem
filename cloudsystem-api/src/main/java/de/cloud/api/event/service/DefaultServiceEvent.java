package de.cloud.api.event.service;

import de.cloud.api.service.CloudService;
import de.cloud.api.event.CloudEvent;
import org.jetbrains.annotations.NotNull;

public abstract class DefaultServiceEvent implements CloudEvent {

    private final CloudService service;

    public DefaultServiceEvent(final @NotNull CloudService service) {
        this.service = service;
    }

    public CloudService getService() {
        return this.service;
    }

}
