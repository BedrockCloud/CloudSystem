package de.cloud.plugin.bootstrap.waterdogpe;

import de.cloud.api.CloudAPI;
import de.cloud.api.bootstrap.waterdogpe.WaterdogCloudBootstrap;
import de.cloud.api.service.CloudService;
import de.cloud.api.service.ServiceState;
import de.cloud.plugin.bootstrap.waterdogpe.handler.JoinHandler;
import de.cloud.plugin.bootstrap.waterdogpe.handler.ReconnectHandler;
import de.cloud.plugin.bootstrap.waterdogpe.listener.WaterdogCloudListener;
import de.cloud.plugin.bootstrap.waterdogpe.listener.WaterdogListener;
import de.cloud.plugin.bootstrap.waterdogpe.commands.WaterdogCloudCommand;
import de.cloud.wrapper.Wrapper;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.event.defaults.*;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.plugin.Plugin;
import dev.waterdog.waterdogpe.plugin.PluginYAML;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;
import java.util.Optional;

public final class WaterdogBootstrap extends Plugin {
    @Getter
    private WaterdogCloudListener cloudListener;
    @Getter
    private static WaterdogBootstrap instance;

    @Override
    public void onStartup() {
        instance = this;
    }

    @Override
    public void onEnable() {
        WaterdogListener listener = new WaterdogListener(this);

        // update that the service is ready to use
        var service = Wrapper.getInstance().thisService();

        if (service.getGroup().isAutoUpdating()) {
            service.setState(ServiceState.ONLINE);
            service.update();
        }

        var waterdogSyncBootstrap = new WaterdogCloudBootstrap();
        waterdogSyncBootstrap.load(this.getProxy());

        this.cloudListener = new WaterdogCloudListener(Wrapper.getInstance());

        getProxy().getEventManager().subscribe(PlayerLoginEvent.class, listener::handle);
        getProxy().getEventManager().subscribe(ServerTransferRequestEvent.class, listener::handle);
        getProxy().getEventManager().subscribe(PlayerDisconnectedEvent.class, listener::handle);
        getProxy().getEventManager().subscribe(ProxyPingEvent.class, listener::handle);
        getProxy().getEventManager().subscribe(InitialServerDeterminedEvent.class, listener::handle);

        getProxy().getCommandMap().registerCommand(new WaterdogCloudCommand());

        ProxyServer.getInstance().setJoinHandler(new JoinHandler(this));
        ProxyServer.getInstance().setReconnectHandler(new ReconnectHandler(this));
    }

    public @NotNull Optional<CloudService> getFallback(final ProxiedPlayer player) {
        return CloudAPI.getInstance().getServiceManager().getAllCachedServices().stream()
            .filter(service -> service.getState().equals(ServiceState.ONLINE))
            .filter(service -> !service.getGroup().getGameServerVersion().isProxy())
            .filter(service -> service.getGroup().isFallbackGroup())
            .filter(service -> (player.getServerInfo() == null || !player.getServerInfo().getServerName().equals(service.getName())))
            .filter(service -> (!service.isFull()))
            .min(Comparator.comparing(CloudService::getOnlineCount));
    }
}
