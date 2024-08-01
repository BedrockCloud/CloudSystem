package de.cloud.api.event.service;

import de.cloud.api.service.CloudService;
import org.jetbrains.annotations.NotNull;

public final class CloudServiceRegisterEvent extends DefaultServiceEvent {

    public CloudServiceRegisterEvent(final @NotNull CloudService service) {
        super(service);
    }

}
