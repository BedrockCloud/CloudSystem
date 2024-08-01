package de.cloud.network.packet;

import io.netty.channel.ChannelHandlerContext;

@FunctionalInterface
public interface PacketListener<T> {
    void handle(ChannelHandlerContext channelHandlerContext, T packet);
}
