package de.cloud.network.server;

import de.cloud.network.server.client.ConnectedClient;
import de.cloud.network.NetworkType;
import de.cloud.network.Node;
import de.cloud.network.packet.Packet;
import de.cloud.network.packet.PacketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NettyServer extends Node {

    private final Map<Channel, ConnectedClient> connectedClients = new ConcurrentHashMap<>();

    private EventLoopGroup bossEventLoopGroup;
    private EventLoopGroup workerEventLoopGroup;
    private ChannelFuture channelFuture;

    public NettyServer(@NotNull final PacketHandler packetHandler, @NotNull final String name, @NotNull final NetworkType networkType) {
        super(packetHandler, name, networkType);
    }

    @Override
    public void connect(@NotNull final String host, final int port) {
        bossEventLoopGroup = newEventLoopGroup(1);
        workerEventLoopGroup = newEventLoopGroup();

        try {
            channelFuture = new ServerBootstrap()
                .channel(getServerSocketChannel())
                .group(bossEventLoopGroup, workerEventLoopGroup)
                .childHandler(new NettyServerInitializer(this))
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.AUTO_READ, true)
                .bind(host, port)
                .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
                .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
                .sync()
                .channel()
                .closeFuture();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (channelFuture != null) {
            channelFuture.cancel(true);
        }

        if (bossEventLoopGroup != null) {
            bossEventLoopGroup.shutdownGracefully();
        }

        if (workerEventLoopGroup != null) {
            workerEventLoopGroup.shutdownGracefully();
        }
    }

    public void addClient(@NotNull final Channel channel, @NotNull final String name, @NotNull final NetworkType networkType) {
        var client = new ConnectedClient(name, channel, networkType);
        connectedClients.put(channel, client);
        switch (networkType) {
            case NODE -> onNodeConnected(client);
            case WRAPPER -> onServiceConnected(client);
        }
    }

    public void closeClient(@NotNull final ChannelHandlerContext channelHandlerContext) {
        var client = connectedClients.remove(channelHandlerContext.channel());
        if (client != null) {
            switch (client.networkType()) {
                case NODE -> onNodeDisconnected(client);
                case WRAPPER -> onServiceDisconnected(client);
            }
            channelHandlerContext.close();
        }
    }

    public ConnectedClient getClient(@NotNull final Channel channel) {
        return connectedClients.get(channel);
    }

    public Optional<ConnectedClient> getClient(@NotNull final String name) {
        return connectedClients.values().stream()
            .filter(client -> client.name().equalsIgnoreCase(name))
            .findFirst();
    }

    public Collection<ConnectedClient> getClients() {
        return connectedClients.values();
    }

    public List<ConnectedClient> getServices() {
        return connectedClients.values().stream()
            .filter(client -> client.networkType() == NetworkType.WRAPPER)
            .toList();
    }

    public List<ConnectedClient> getNodes() {
        return connectedClients.values().stream()
            .filter(client -> client.networkType() == NetworkType.NODE)
            .toList();
    }

    public void sendPacketToAll(@NotNull final Packet packet) {
        connectedClients.keySet().forEach(channel -> channel.writeAndFlush(packet));
    }

    public void sendPacketToType(@NotNull final Packet packet, @NotNull final NetworkType networkType) {
        connectedClients.values().stream()
            .filter(client -> client.networkType() == networkType)
            .forEach(client -> client.channel().writeAndFlush(packet));
    }

    public abstract void onNodeConnected(@NotNull final ConnectedClient connectedClient);

    public abstract void onNodeDisconnected(@NotNull final ConnectedClient connectedClient);

    public abstract void onServiceConnected(@NotNull final ConnectedClient connectedClient);

    public abstract void onServiceDisconnected(@NotNull final ConnectedClient connectedClient);
}
