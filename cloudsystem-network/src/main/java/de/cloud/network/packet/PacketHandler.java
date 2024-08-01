package de.cloud.network.packet;

import io.netty.channel.ChannelHandlerContext;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public final class PacketHandler {

    private final List<Class<? extends Packet>> packets;
    @Getter
    private final Map<UUID, Consumer<Packet>> responses = new HashMap<>();
    private final Map<Class<? extends Packet>, List<PacketListener<?>>> packetListeners = new HashMap<>();

    @SafeVarargs
    public PacketHandler(final Class<? extends Packet>... packets) {
        this.packets = List.of(packets); // Use List.of for immutable list
    }

    public <T extends Packet> void registerPacketListener(@NotNull Class<T> packetClass, @NotNull PacketListener<T> listener) {
        packetListeners.computeIfAbsent(packetClass, k -> new ArrayList<>()).add(listener);
    }

    public void call(ChannelHandlerContext ctx, Object packet) {
        var packetListenersForClass = packetListeners.get(packet.getClass());
        if (packetListenersForClass != null) {
            for (var listener : packetListenersForClass) {
                @SuppressWarnings("unchecked")
                var typedListener = (PacketListener<Object>) listener;
                typedListener.handle(ctx, packet);
            }
        }
    }

    public int getPacketId(@NotNull Class<?> clazz) {
        return packets.indexOf(clazz);
    }

    public Class<? extends Packet> getPacketClass(int id) {
        return packets.get(id);
    }
}
