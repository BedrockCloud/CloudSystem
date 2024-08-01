package de.cloud.plugin.bootstrap.nukkit;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import de.cloud.api.service.ServiceState;
import de.cloud.plugin.bootstrap.nukkit.commands.CloudPlayerInfoCommand;
import de.cloud.plugin.bootstrap.nukkit.listener.NukkitListener;
import de.cloud.wrapper.Wrapper;
import de.cloud.plugin.bootstrap.nukkit.commands.CloudTransferCommand;

public final class NukkitBootstrap extends PluginBase {

    @Override
    public void onEnable() {
        // update that the service is ready to use
        final var service = Wrapper.getInstance().thisService();

        if (service.getGroup().isAutoUpdating()) {
            service.setState(ServiceState.ONLINE);
            service.update();
        }

        Server.getInstance().getCommandMap().register("transfer", new CloudTransferCommand());
        Server.getInstance().getCommandMap().register("playerinfo", new CloudPlayerInfoCommand());
        Server.getInstance().getPluginManager().registerEvents(new NukkitListener(), this);
    }

}
