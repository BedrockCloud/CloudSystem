package de.cloud.api.bootstrap.waterdogpe;

import de.cloud.api.CloudAPI;
import de.cloud.api.network.packet.player.CloudPlayerKickPacket;
import de.cloud.api.network.packet.player.CloudPlayerMessagePacket;
import de.cloud.api.network.packet.player.CloudPlayerSendServicePacket;
import dev.waterdog.waterdogpe.ProxyServer;

public class WaterdogCloudBootstrap {

    public void load(ProxyServer server) {
        CloudAPI.getInstance().getPacketHandler().registerPacketListener(CloudPlayerKickPacket.class, (channelHandlerContext, packet) -> {
            var player = server.getPlayer(packet.getUuid());
            assert player != null;
            player.disconnect(packet.getReason());
        });
        CloudAPI.getInstance().getPacketHandler().registerPacketListener(CloudPlayerMessagePacket.class, (channelHandlerContext, packet) -> {
            var player = server.getPlayer(packet.getUuid());
            assert player != null;
            player.sendMessage(packet.getMessage());
        });
        CloudAPI.getInstance().getPacketHandler().registerPacketListener(CloudPlayerSendServicePacket.class, (channelHandlerContext, packet) -> {
            var player = server.getPlayer(packet.getUuid());
            assert player != null;
            if (player.getServerInfo() != null && player.getServerInfo().getServerName().equals(packet.getService())) return;
            player.connect(server.getServerInfo(packet.getService()));
        });

        server.getLogger().info("Loaded " + this.getClass().getSimpleName() + " successfully.");
    }
}
