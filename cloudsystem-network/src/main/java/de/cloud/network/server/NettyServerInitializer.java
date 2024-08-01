package de.cloud.network.server;

import de.cloud.network.codec.PacketDecoder;
import de.cloud.network.codec.PacketEncoder;
import de.cloud.network.codec.PacketLengthDeserializer;
import de.cloud.network.codec.PacketLengthSerializer;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public final class NettyServerInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyServer nettyServer;

    public NettyServerInitializer(final NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline()
            .addLast("packet-length-deserializer", new PacketLengthDeserializer())
            .addLast("packet-decoder", new PacketDecoder(this.nettyServer.getPacketHandler()))
            .addLast("packet-length-serializer", new PacketLengthSerializer())
            .addLast("packet-encoder", new PacketEncoder(this.nettyServer.getPacketHandler()))
            .addLast("handler", new NettyServerHandler(this.nettyServer));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
    }

}
