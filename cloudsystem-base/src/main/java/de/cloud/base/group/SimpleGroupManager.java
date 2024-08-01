package de.cloud.base.group;

import de.cloud.api.groups.ServiceGroup;
import de.cloud.api.groups.impl.AbstractGroupManager;
import de.cloud.api.groups.impl.SimpleServiceGroup;
import de.cloud.api.network.packet.QueryPacket;
import de.cloud.api.network.packet.group.ServiceGroupExecutePacket;
import de.cloud.api.network.packet.group.ServiceGroupUpdatePacket;
import de.cloud.api.version.GameServerVersion;
import de.cloud.base.Base;
import de.cloud.database.CloudDatabaseProvider;
import de.cloud.network.NetworkType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class SimpleGroupManager extends AbstractGroupManager {

    private final CloudDatabaseProvider database;

    public SimpleGroupManager() {
        final var base = Base.getInstance();

        this.database = base.getDatabaseManager().getProvider();

        // loading all database groups
        super.allCachedServiceGroups = this.database.getAllServiceGroups();

        if (!this.allCachedServiceGroups.isEmpty()) {
            base.getLogger().log("§7Loading following groups: §b"
                + this.getAllCachedServiceGroups().stream()
                .map(ServiceGroup::getName)
                .collect(Collectors.joining("§7, §b")));
        } else {
            base.getWorkerThread().addRunnable(() -> {
                Base.getInstance().getLogger().log("0 groups loaded, should default groups be created? (yes/no)");
                Base.getInstance().getConsoleManager().addInput(input -> {
                    if (input.startsWith("yes")) {
                        this.addServiceGroup(new SimpleServiceGroup("Proxy", "Proxy",
                            Base.getInstance().getNode().getName(), "A default cloud service", 512,
                            100, 1, -1, false, false,
                            false, true, GameServerVersion.WATERDOG));
                        this.addServiceGroup(new SimpleServiceGroup("Lobby", "Lobby",
                            Base.getInstance().getNode().getName(), "A default cloud service", 1024,
                            100, 1, -1, false, true,
                            false, true, GameServerVersion.NUKKIT));
                        Base.getInstance().getLogger().log("§7You created following groups: §b" + "Lobby (Nukkit)§7, §bProxy (Waterdog)");
                    }
                }, Arrays.asList("yes", "no"));
            });
        }
    }

    @Override
    public void addServiceGroup(final @NotNull ServiceGroup serviceGroup) {
        this.database.addGroup(serviceGroup);
        Base.getInstance().getNode().sendPacketToAll(new ServiceGroupExecutePacket(serviceGroup, ServiceGroupExecutePacket.Executor.CREATE));
        super.addServiceGroup(serviceGroup);
    }


    @Override
    public void removeServiceGroup(final @NotNull ServiceGroup serviceGroup) {
        this.database.removeGroup(serviceGroup);
        Base.getInstance().getNode().sendPacketToAll(new ServiceGroupExecutePacket(serviceGroup, ServiceGroupExecutePacket.Executor.REMOVE));
        super.removeServiceGroup(serviceGroup);
    }

    @Override
    public void updateServiceGroup(@NotNull ServiceGroup serviceGroup) {
        final var packet = new ServiceGroupUpdatePacket(serviceGroup);
        // update all other nodes and this service groups
        Base.getInstance().getNode().sendPacketToType(new QueryPacket(packet, QueryPacket.QueryState.SECOND_RESPONSE), NetworkType.NODE);
        // update own service group caches
        Base.getInstance().getNode().sendPacketToType(packet, NetworkType.WRAPPER);
    }

}
