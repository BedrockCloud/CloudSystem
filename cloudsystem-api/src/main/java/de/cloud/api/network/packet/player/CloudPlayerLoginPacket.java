package de.cloud.api.network.packet.player;

import de.cloud.network.packet.Packet;
import de.cloud.network.packet.NetworkBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class CloudPlayerLoginPacket implements Packet {

    private String address;
    private String username;
    private UUID uuid;
    private String proxyServer;

    @Override
    public void read(@NotNull NetworkBuf byteBuf) {
        this.address = byteBuf.readString();
        this.username = byteBuf.readString();
        this.uuid = byteBuf.readUUID();
        this.proxyServer = byteBuf.readString();
    }

    @Override
    public void write(@NotNull NetworkBuf byteBuf) {
        byteBuf
            .writeString(this.address)
            .writeString(this.username)
            .writeUUID(this.uuid)
            .writeString(this.proxyServer);
    }

}
