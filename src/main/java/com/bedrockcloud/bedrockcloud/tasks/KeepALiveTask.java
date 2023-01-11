package com.bedrockcloud.bedrockcloud.tasks;

import com.bedrockcloud.bedrockcloud.api.GroupAPI;
import com.bedrockcloud.bedrockcloud.api.MessageAPI;
import com.bedrockcloud.bedrockcloud.server.proxy.ProxyServer;
import com.bedrockcloud.bedrockcloud.serverQuery.api.Protocol;
import com.bedrockcloud.bedrockcloud.serverQuery.api.QueryException;
import com.bedrockcloud.bedrockcloud.serverQuery.api.QueryStatus;
import com.bedrockcloud.bedrockcloud.templates.Template;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import com.bedrockcloud.bedrockcloud.server.gameserver.GameServer;
import java.io.File;
import java.util.TimerTask;

import com.bedrockcloud.bedrockcloud.network.packets.KeepALivePacket;
import com.bedrockcloud.bedrockcloud.BedrockCloud;

/*
 * KeepALiveTask to check the status of a service
 */
public class KeepALiveTask extends Thread {
    @Override
    public void run() {
        final Iterator<String> var1 = BedrockCloud.getGameServerProvider().gameServerMap.keySet().iterator();
        while (BedrockCloud.isRunning()) {
            try {
                if (!var1.hasNext()) return;

                final String servername = var1.next();
                if (servername == null) {
                    return;
                }
                final GameServer gameServer = BedrockCloud.getGameServerProvider().getGameServer(servername);
                if (gameServer == null) {
                    return;
                }

                try {
                    try {
                        new QueryStatus.Builder("127.0.0.1")
                                .setProtocol(Protocol.UDP_FULL)
                                .setPort(gameServer.getServerPort())
                                .build()
                                .getStatus()
                                .toJson();
                    } catch (IllegalArgumentException ignored) {
                    }
                } catch (QueryException e) {
                    final Template group = gameServer.getTemplate();
                    if (gameServer.getAliveChecks() == 0) {
                        final KeepALivePacket packet = new KeepALivePacket();
                        gameServer.pushPacket(packet);
                    } else {
                        if (BedrockCloud.getGameServerProvider().existServer(servername)) {
                            if (gameServer.getAliveChecks() >= 10) {
                                gameServer.setAliveChecks(0);

                                String notifyMessage = MessageAPI.timedOut.replace("%service", servername);
                                BedrockCloud.sendNotifyCloud(notifyMessage);
                                BedrockCloud.getLogger().warning(notifyMessage);

                                final ProcessBuilder builder = new ProcessBuilder();
                                try {
                                    builder.command("/bin/sh", "-c", "screen -X -S " + servername + " kill").start();
                                } catch (Exception ignored) {
                                }
                                try {
                                    builder.command("/bin/sh", "-c", "kill " + gameServer.pid).start();
                                } catch (Exception ignored) {
                                }
                                try {
                                    BedrockCloud.getGameServerProvider().deleteServer(new File("./temp/" + servername), servername);
                                } catch (NullPointerException ex) {
                                    BedrockCloud.getLogger().exception(ex);
                                }

                                gameServer.getTemplate().removeServer(servername);
                                BedrockCloud.getGameServerProvider().removeServer(servername);

                                if (group != null) {
                                    if (BedrockCloud.getTemplateProvider().isTemplateRunning(group)) {
                                        if (gameServer.getTemplate().getRunningTemplateServers().size() < gameServer.getTemplate().getMinRunningServer()) {
                                            if (gameServer.getTemplate().getRunningTemplateServers().size() < gameServer.getTemplate().getMaxRunningServer()) {
                                                if (gameServer.getTemplate().getType() == GroupAPI.POCKETMINE_SERVER) {
                                                    new GameServer(gameServer.getTemplate());
                                                } else {
                                                    if (gameServer.getTemplate().getType() == GroupAPI.PROXY_SERVER) {
                                                        new ProxyServer(gameServer.getTemplate());
                                                    }
                                                }
                                            } else {
                                                BedrockCloud.getLogger().error("§cCan't start more servers of the group §e" + gameServer.getTemplate().getName());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (gameServer.getAliveChecks() < 10) {
                        gameServer.setAliveChecks(gameServer.getAliveChecks() + 1);
                    }
                }
            } catch (ConcurrentModificationException ignored) {
            }
        }
    }
}