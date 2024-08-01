package de.cloud.api.network.packet.group;

import de.cloud.api.groups.ServiceGroup;
import de.cloud.network.packet.NetworkBuf;
import de.cloud.network.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ServiceGroupCacheUpdatePacket implements Packet {

    private List<ServiceGroup> groups;

    @Override
    public void write(@NotNull NetworkBuf byteBuf) {
        byteBuf.writeInt(this.groups.size());
        this.groups.forEach(group -> group.write(byteBuf));
    }

    @Override
    public void read(@NotNull NetworkBuf byteBuf) {
        var amount = byteBuf.readInt();
        this.groups = new ArrayList<>();
        for (var i = 0; i < amount; i++) {
            this.groups.add(ServiceGroup.read(byteBuf));
        }
    }

}
