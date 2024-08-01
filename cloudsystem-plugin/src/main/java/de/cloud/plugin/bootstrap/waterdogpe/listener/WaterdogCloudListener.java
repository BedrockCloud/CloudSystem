package de.cloud.plugin.bootstrap.waterdogpe.listener;

import de.cloud.api.CloudAPI;
import de.cloud.api.event.service.CloudServiceRegisterEvent;
import de.cloud.api.event.service.CloudServiceRemoveEvent;
import de.cloud.api.network.packet.player.CloudPlayerKickPacket;
import de.cloud.api.network.packet.player.CloudPlayerMessagePacket;
import de.cloud.api.network.packet.player.CloudPlayerSendServicePacket;
import de.cloud.api.service.CloudService;
import de.cloud.network.packet.PacketListener;
import de.cloud.wrapper.Wrapper;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.serverinfo.BedrockServerInfo;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;

public class WaterdogCloudListener {

    public WaterdogCloudListener(Wrapper wrapper) {
        // load all current groups
        for (final var allCachedService : wrapper.getServiceManager().getAllCachedServices()) {
            if (!allCachedService.getGroup().getGameServerVersion().isProxy()) registerService(allCachedService);
        }

        CloudAPI.getInstance().getEventHandler().registerEvent(CloudServiceRegisterEvent.class, event -> {
            if (!event.getService().getGroup().getGameServerVersion().isProxy())
                registerService(event.getService());
        });

        CloudAPI.getInstance().getEventHandler().registerEvent(CloudServiceRemoveEvent.class, event -> {
            unregisterService(event.getService());
        });
    }

    private void registerService(String name, InetSocketAddress socketAddress) {
        ProxyServer.getInstance().registerServerInfo(new BedrockServerInfo(name, socketAddress, null));
    }

    public void unregisterService(String name) {
        ProxyServer.getInstance().getServerInfoMap().remove(name);
    }

    public void registerService(@NotNull CloudService service) {
        this.registerService(service.getName(), new InetSocketAddress(service.getHostName(), service.getPort()));
    }
}
