package de.cloud.api.event.group;

import de.cloud.api.groups.ServiceGroup;
import org.jetbrains.annotations.NotNull;

public class CloudServiceGroupUpdateEvent extends DefaultServiceGroupEvent {

    public CloudServiceGroupUpdateEvent(final @NotNull ServiceGroup serviceGroup) {
        super(serviceGroup);
    }

}
