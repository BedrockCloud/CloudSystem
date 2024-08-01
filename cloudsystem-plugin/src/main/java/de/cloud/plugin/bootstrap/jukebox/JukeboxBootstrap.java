package de.cloud.plugin.bootstrap.jukebox;

import de.cloud.api.service.ServiceState;
import de.cloud.plugin.bootstrap.jukebox.commands.CloudPlayerInfoCommand;
import de.cloud.plugin.bootstrap.jukebox.commands.CloudTransferCommand;
import de.cloud.plugin.bootstrap.jukebox.listener.JukeboxListener;
import de.cloud.wrapper.Wrapper;
import org.jukeboxmc.api.plugin.Plugin;

public class JukeboxBootstrap extends Plugin {

    @Override
    public void onEnable() {
        // update that the service is ready to use
        final var service = Wrapper.getInstance().thisService();

        if (service.getGroup().isAutoUpdating()) {
            service.setState(ServiceState.ONLINE);
            service.update();
        }

        this.getServer().getCommandManager().registerCommand(new CloudTransferCommand());
        this.getServer().getCommandManager().registerCommand(new CloudPlayerInfoCommand());
        this.getServer().getPluginManager().registerListener(new JukeboxListener());
    }
}
