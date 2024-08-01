package de.cloud.api.network.packet.service;

import de.cloud.network.packet.NetworkBuf;
import de.cloud.network.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMemoryRequest implements Packet {

    private int memory;

    @Override
    public void write(@NotNull NetworkBuf networkBuf) {
        networkBuf.writeInt(memory);
    }

    @Override
    public void read(@NotNull NetworkBuf networkBuf) {
        this.memory = networkBuf.readInt();
    }
}
