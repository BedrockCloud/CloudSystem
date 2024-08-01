package de.cloud.network.client;

import de.cloud.network.codec.PacketDecoder;
import de.cloud.network.codec.PacketEncoder;
import de.cloud.network.codec.PacketLengthDeserializer;
import de.cloud.network.codec.PacketLengthSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public final class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    private final NettyClient nettyClient;

    public NettyClientInitializer(final NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline()
            .addLast("packet-length-deserializer", new PacketLengthDeserializer())
            .addLast("packet-decoder", new PacketDecoder(this.nettyClient.getPacketHandler()))
            .addLast("packet-length-serializer", new PacketLengthSerializer())
            .addLast("packet-encoder", new PacketEncoder(this.nettyClient.getPacketHandler()))
            .addLast("handler", new NettyClientHandler(this.nettyClient));
    }

}
