package de.cloud.plugin.bootstrap.waterdogpe.handler;

import de.cloud.api.CloudAPI;
import de.cloud.plugin.bootstrap.waterdogpe.WaterdogBootstrap;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.connection.handler.IReconnectHandler;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;

import java.util.concurrent.atomic.AtomicReference;

public class ReconnectHandler implements IReconnectHandler {
    private final WaterdogBootstrap bootstrap;

    public ReconnectHandler(WaterdogBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public ServerInfo getFallbackServer(ProxiedPlayer proxiedPlayer, ServerInfo serverInfo, String s) {
        AtomicReference<ServerInfo> fallbackServerInfo = new AtomicReference<>(null);
        CloudAPI.getInstance().getPlayerManager().getCloudPlayer(proxiedPlayer.getUniqueId()).ifPresent(cloudPlayer -> {
            this.bootstrap.getFallback(proxiedPlayer)
                .map(service -> ProxyServer.getInstance().getServerInfo(service.getName()))
                .ifPresentOrElse(
                    fallbackServerInfo::set,
                    () -> proxiedPlayer.disconnect("§cNo fallback could be found.")
                );
        });
        return fallbackServerInfo.get() != null ? fallbackServerInfo.get() : null;
    }
}
