package de.cloud.api.event.service;

import de.cloud.api.event.CloudEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CloudServiceRemoveEvent implements CloudEvent {

    private String service;

}
