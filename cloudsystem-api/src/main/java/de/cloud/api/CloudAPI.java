package de.cloud.api;

import de.cloud.api.event.EventHandler;
import de.cloud.api.event.SimpleEventHandler;
import de.cloud.api.groups.GroupManager;
import de.cloud.api.logger.Logger;
import de.cloud.api.network.packet.CustomPacket;
import de.cloud.api.network.packet.QueryPacket;
import de.cloud.api.network.packet.RedirectPacket;
import de.cloud.api.network.packet.ResponsePacket;
import de.cloud.api.network.packet.group.ServiceGroupCacheUpdatePacket;
import de.cloud.api.network.packet.group.ServiceGroupExecutePacket;
import de.cloud.api.network.packet.group.ServiceGroupUpdatePacket;
import de.cloud.api.network.packet.init.CacheInitPacket;
import de.cloud.api.network.packet.player.*;
import de.cloud.api.network.packet.service.*;
import de.cloud.api.player.PlayerManager;
import de.cloud.api.service.ServiceManager;
import de.cloud.network.packet.PacketHandler;
import de.cloud.network.packet.auth.NodeHandshakeAuthenticationPacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class CloudAPI {

    @Getter
    protected static CloudAPI instance;
    @Getter
    protected Logger logger;

    private final CloudAPIType cloudAPITypes;
    protected final PacketHandler packetHandler;
    protected final EventHandler eventHandler;

    protected CloudAPI(final CloudAPIType cloudAPIType) {
        instance = this;

        this.cloudAPITypes = cloudAPIType;
        this.packetHandler = new PacketHandler(
            NodeHandshakeAuthenticationPacket.class, QueryPacket.class, RedirectPacket.class, CustomPacket.class, ResponsePacket.class, ServiceMemoryRequest.class,
            ServiceGroupCacheUpdatePacket.class, ServiceGroupExecutePacket.class, ServiceGroupUpdatePacket.class,
            CacheInitPacket.class, CloudPlayerDisconnectPacket.class, CloudPlayerKickPacket.class,
            CloudPlayerLoginPacket.class, CloudPlayerMessagePacket.class, CloudPlayerSendServicePacket.class,
            CloudPlayerUpdatePacket.class, ServiceAddPacket.class, ServiceRemovePacket.class,
            ServiceRequestShutdownPacket.class, ServiceUpdatePacket.class, ServiceCopyRequestPacket.class, ServiceStartPacket.class);
        this.eventHandler = new SimpleEventHandler();
    }

    /**
     * @return the group manager
     */
    public abstract @NotNull GroupManager getGroupManager();

    /**
     * @return the service manager
     */
    public abstract @NotNull ServiceManager getServiceManager();

    /**
     * @return the player manager
     */
    public abstract @NotNull PlayerManager getPlayerManager();

}

