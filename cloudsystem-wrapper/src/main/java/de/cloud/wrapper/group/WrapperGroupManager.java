package de.cloud.wrapper.group;

import de.cloud.api.groups.ServiceGroup;
import de.cloud.api.groups.impl.AbstractGroupManager;
import de.cloud.api.network.packet.QueryPacket;
import de.cloud.api.network.packet.group.ServiceGroupUpdatePacket;
import de.cloud.wrapper.Wrapper;
import org.jetbrains.annotations.NotNull;

public final class WrapperGroupManager extends AbstractGroupManager {

    @Override
    public void updateServiceGroup(@NotNull ServiceGroup serviceGroup) {
        Wrapper.getInstance().getClient().sendPacket(new QueryPacket(new ServiceGroupUpdatePacket(serviceGroup), QueryPacket.QueryState.FIRST_RESPONSE));
    }

}
