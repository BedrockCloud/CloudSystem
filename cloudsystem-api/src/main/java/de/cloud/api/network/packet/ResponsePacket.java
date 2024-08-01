package de.cloud.api.network.packet;

import de.cloud.api.CloudAPI;
import de.cloud.network.packet.NetworkBuf;
import de.cloud.network.packet.Packet;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.function.Consumer;

@Getter
@NoArgsConstructor
public class ResponsePacket implements Packet {

    private UUID uuid;
    private Packet packet;
    private Consumer<Packet> onResponse;

    public ResponsePacket(Packet packet, Consumer<Packet> onResponse) {
        this.uuid = UUID.randomUUID();
        this.packet = packet;
        this.onResponse = onResponse;
    }

    @Override
    public void write(@NotNull NetworkBuf networkBuf) {
        networkBuf.writeUUID(uuid).writeInt(CloudAPI.getInstance().getPacketHandler().getPacketId(this.packet.getClass()));
        packet.write(networkBuf);
        CloudAPI.getInstance().getPacketHandler().getResponses().put(uuid, onResponse);
    }

    @Override
    public void read(@NotNull NetworkBuf networkBuf) {
        this.uuid = networkBuf.readUUID();
        int id = networkBuf.readInt();
        this.initPacket(networkBuf, CloudAPI.getInstance().getPacketHandler().getPacketClass(id));
    }

    public void initPacket(NetworkBuf byteBuf, Class<? extends Packet> it) {
        try {
            this.packet = it.getConstructor().newInstance();
            this.packet.read(byteBuf);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
