package de.cloud.wrapper.network;

import de.cloud.api.CloudAPI;
import de.cloud.network.NetworkType;
import de.cloud.network.client.NettyClient;
import de.cloud.network.packet.PacketHandler;
import io.netty.channel.ChannelHandlerContext;

public final class WrapperClient extends NettyClient {

    public WrapperClient(final PacketHandler packetHandler, final String name, final String hostname, final int port) {
        super(packetHandler, name, NetworkType.WRAPPER);

        this.connect(hostname, port);
        CloudAPI.getInstance().getLogger().log("§7The service started successfully network service.");
    }

    @Override
    public void onActivated(ChannelHandlerContext channelHandlerContext) {
        CloudAPI.getInstance().getLogger().log("This service successfully connected to the cluster.");
    }

    @Override
    public void onClose(ChannelHandlerContext channelHandlerContext) {
        CloudAPI.getInstance().getLogger().log("This service disconnected from the cluster");
    }

}
