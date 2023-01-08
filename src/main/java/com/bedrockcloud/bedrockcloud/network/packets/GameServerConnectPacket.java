package com.bedrockcloud.bedrockcloud.network.packets;

import com.bedrockcloud.bedrockcloud.BedrockCloud;
import com.bedrockcloud.bedrockcloud.network.DataPacket;
import com.bedrockcloud.bedrockcloud.network.client.ClientRequest;
import com.bedrockcloud.bedrockcloud.server.gameserver.GameServer;
import com.bedrockcloud.bedrockcloud.api.MessageAPI;
import com.bedrockcloud.bedrockcloud.server.privategameserver.PrivateGameServer;
import com.bedrockcloud.bedrockcloud.server.proxy.ProxyServer;
import org.json.simple.JSONObject;

import java.net.DatagramSocket;
import java.net.SocketException;

public class GameServerConnectPacket extends DataPacket
{
    @Override
    public void handle(final JSONObject jsonObject, final ClientRequest clientRequest) {
        final String serverName = jsonObject.get("serverName").toString();
        final String serverPort = jsonObject.get("serverPort").toString();
        final String serverPid = jsonObject.get("serverPid").toString();
        final boolean isPrivate = Boolean.parseBoolean(jsonObject.get("isPrivate").toString());
        if (!isPrivate) {
            final GameServer gameServer = BedrockCloud.getGameServerProvider().getGameServer(serverName);
            gameServer.setSocket(clientRequest.getSocket());
            gameServer.pid = Integer.parseInt(serverPid);
            gameServer.setAliveChecks(0);
            final VersionInfoPacket versionInfoPacket = new VersionInfoPacket();
            gameServer.pushPacket(versionInfoPacket);
            for (final String key : BedrockCloud.getProxyServerProvider().getProxyServerMap().keySet()) {
                final ProxyServer proxy = BedrockCloud.getProxyServerProvider().getProxyServer(key);
                final RegisterServerPacket packet = new RegisterServerPacket();
                packet.addValue("serverPort", serverPort);
                packet.addValue("serverName", serverName);
                proxy.pushPacket(packet);
            }

            String notifyMessage = MessageAPI.startedMessage.replace("%service", serverName);
            BedrockCloud.sendNotifyCloud(notifyMessage);
            BedrockCloud.getLogger().warning(notifyMessage);

            gameServer.getTemplate().addServer(gameServer.getTemplate(), serverName);
        } else {
            final PrivateGameServer gameServer = BedrockCloud.getPrivateGameServerProvider().getGameServer(serverName);
            gameServer.setSocket(clientRequest.getSocket());
            gameServer.pid = Integer.parseInt(serverPid);
            gameServer.setAliveChecks(0);
            final VersionInfoPacket versionInfoPacket = new VersionInfoPacket();
            gameServer.pushPacket(versionInfoPacket);
            for (final String key : BedrockCloud.getProxyServerProvider().getProxyServerMap().keySet()) {
                final ProxyServer proxy = BedrockCloud.getProxyServerProvider().getProxyServer(key);
                final RegisterServerPacket packet = new RegisterServerPacket();
                packet.addValue("serverPort", serverPort);
                packet.addValue("serverName", serverName);
                proxy.pushPacket(packet);
            }

            String notifyMessage = MessageAPI.startedMessage.replace("%service", serverName);
            BedrockCloud.sendNotifyCloud(notifyMessage);
            BedrockCloud.getLogger().warning(notifyMessage);

            gameServer.getTemplate().addServer(gameServer.getTemplate(), serverName);

            PlayerMovePacket pk = new PlayerMovePacket();
            pk.playerName = gameServer.getServerOwner();
            pk.toServer = pk.getServerName();
            for (final ProxyServer proxy : BedrockCloud.getProxyServerProvider().getProxyServerMap().values()) {
                proxy.pushPacket(pk);
            }
        }
    }
}