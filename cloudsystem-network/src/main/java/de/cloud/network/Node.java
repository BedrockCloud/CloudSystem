package de.cloud.network;

import de.cloud.network.packet.PacketHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public abstract class Node {

    private static final boolean USE_EPOLL = Epoll.isAvailable();

    protected final PacketHandler packetHandler;
    protected final String name;
    protected final NetworkType networkType;

    protected Node(final PacketHandler packetHandler, final String name, final NetworkType networkType) {
        this.packetHandler = packetHandler;
        this.name = name;
        this.networkType = networkType;
    }

    public abstract void connect(@NotNull String host, int port);

    public abstract void close();

    protected EventLoopGroup newEventLoopGroup() {
        return newEventLoopGroup(0);
    }

    protected EventLoopGroup newEventLoopGroup(final int threads) {
        return USE_EPOLL ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
    }

    protected Class<? extends ServerSocketChannel> getServerSocketChannel() {
        return USE_EPOLL ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    protected Class<? extends SocketChannel> getSocketChannel() {
        return USE_EPOLL ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}
