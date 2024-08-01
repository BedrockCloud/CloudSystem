package de.cloud.api.network.packet.player;

import de.cloud.api.service.CloudService;
import de.cloud.api.CloudAPI;
import de.cloud.api.event.player.CloudPlayerUpdateEvent;
import de.cloud.api.player.CloudPlayer;
import de.cloud.network.packet.Packet;
import de.cloud.network.packet.NetworkBuf;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class CloudPlayerUpdatePacket implements Packet {

    private UUID uuid;
    private CloudService server;
    private CloudPlayerUpdateEvent.UpdateReason updateReason;

    public CloudPlayerUpdatePacket(@NotNull CloudPlayer cloudPlayer, @NotNull CloudPlayerUpdateEvent.UpdateReason updateReason) {
        this.uuid = cloudPlayer.getUniqueId();
        this.server = cloudPlayer.getServer();
        this.updateReason = updateReason;
    }

    @Override
    public void write(@NotNull NetworkBuf byteBuf) {
        byteBuf.writeUUID(this.uuid);
        byteBuf.writeString(this.server.getName());
        byteBuf.writeEnum(this.updateReason);
    }

    @Override
    public void read(@NotNull NetworkBuf byteBuf) {
        this.uuid = byteBuf.readUUID();
        this.server = CloudAPI.getInstance().getServiceManager().getServiceByNameOrNull(byteBuf.readString());
        this.updateReason = byteBuf.readEnum();
    }

}
