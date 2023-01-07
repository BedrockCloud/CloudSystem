package com.bedrockcloud.bedrockcloud.server.gameserver;

import java.io.*;

import com.bedrockcloud.bedrockcloud.BedrockCloud;
import com.bedrockcloud.bedrockcloud.network.client.ClientRequest;
import com.bedrockcloud.bedrockcloud.server.port.PortValidator;
import com.bedrockcloud.bedrockcloud.api.MessageAPI;
import com.bedrockcloud.bedrockcloud.network.DataPacket;
import com.bedrockcloud.bedrockcloud.network.packets.GameServerDisconnectPacket;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.bedrockcloud.bedrockcloud.templates.Template;

public class GameServer
{
    private final Template template;
    private final String serverName;
    private final int serverPort;
    public int pid;
    public int state;
    private int playerCount;
    private int aliveChecks;
    private Socket socket;
    public final String temp_path = "./templates/";
    public final String servers_path = "./temp/";
    
    public GameServer(final Template template) {
        this.template = template;
        this.aliveChecks = 0;
        this.serverName = template.getName() + "-" + BedrockCloud.getGameServerProvider().getFreeNumber("./temp/" + template.getName());
        this.serverPort = PortValidator.getNextServerPort(this);
        this.playerCount = 0;
        this.state = 0;
        this.pid = -1;
        BedrockCloud.getGameServerProvider().addGameServer(this);
        this.copyServer();
        try {
            this.startServer();
        }
        catch (InterruptedException e) {
            BedrockCloud.getLogger().exception(e);
        }
    }

    public int getPid() {
        return this.pid;
    }
    
    public String getServerName() {
        return this.serverName;
    }
    
    public int getServerPort() {
        return this.serverPort;
    }
    
    public int getAliveChecks() {
        return this.aliveChecks;
    }
    
    public void setAliveChecks(final int aliveChecks) {
        this.aliveChecks = aliveChecks;
    }
    
    public void startServer() throws InterruptedException {
        final File server = new File("./temp/" + this.serverName);
        if (server.exists()) {
            final ProcessBuilder builder = new ProcessBuilder(new String[0]);

            String notifyMessage = MessageAPI.startMessage.replace("%service", serverName);
            BedrockCloud.sendNotifyCloud(notifyMessage);
            BedrockCloud.getLogger().info(notifyMessage);
            try {
                builder.command("/bin/sh", "-c", "screen -X -S " + this.serverName + " kill").start();
            } catch (Exception e) {
                BedrockCloud.getLogger().exception(e);
            }
            Thread.sleep(1000L);
            try {
                builder.command("/bin/sh", "-c", "screen -dmS " + this.serverName + " ../../bin/php7/bin/php PocketMine-MP.phar").directory(new File("./temp/" + this.serverName)).start();
            } catch (Exception e) {
                BedrockCloud.getLogger().exception(e);
            }
        } else {
            String notifyMessage = MessageAPI.startFailed.replace("%service", serverName);
            BedrockCloud.sendNotifyCloud(notifyMessage);
            BedrockCloud.getLogger().error(notifyMessage);
        }
    }
    
    public void copyServer() {
        final File src = new File("./templates/" + this.template.getName() + "/");
        final File dest = new File("./temp/" + this.serverName);
        BedrockCloud.getGameServerProvider().copy(src, dest);
        final File global_plugins = new File("./local/plugins/pocketmine");
        final File dest_plugs = new File("./temp/" + this.serverName + "/plugins/");
        BedrockCloud.getGameServerProvider().copy(global_plugins, dest_plugs);
        final File file = new File("./local/versions/pocketmine");
        final File dest_lib = new File("./temp/" + this.serverName + "/");
        BedrockCloud.getGameServerProvider().copy(file, dest_lib);

        try {
            Thread.sleep(200L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BedrockCloud.getGameServerProvider().createProperties(this);
    }
    
    public Template getTemplate() {
        return this.template;
    }
    
    public void stopServer() {
        String notifyMessage = MessageAPI.stopMessage.replace("%service", this.serverName);
        BedrockCloud.sendNotifyCloud(notifyMessage);
        BedrockCloud.getLogger().info(notifyMessage);

        final GameServerDisconnectPacket packet = new GameServerDisconnectPacket();
        packet.addValue("reason", "Server Stopped");
        this.pushPacket(packet);
    }

    public void killWithPID() throws IOException {
        final ProcessBuilder builder = new ProcessBuilder();
        try {
            builder.command("/bin/sh", "-c", "screen -X -S " + this.serverName + " kill").start();
        } catch (Exception ignored) {
        }
        try {
            builder.command("/bin/sh", "-c", "kill " + this.pid).start();
        } catch (Exception ignored) {
        }
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(final Socket socket) {
        this.socket = socket;
    }

    public void pushPacket(final DataPacket cloudPacket) {

        if (this.serverName == null || this.socket == null) {
            return;
        }

        if (this.socket.isClosed()) {
            BedrockCloud.getLogger().error("CloudPacket cannot be push because socket is closed.");
            return;
        }

        if (!this.socket.isConnected()) {
            BedrockCloud.getLogger().error("CloudPacket cannot be push because socket is not connected.");
            return;
        }
        try (final BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.getSocket().getOutputStream()))) {
            bufferedWriter.write(cloudPacket.encode());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            BedrockCloud.getLogger().exception(e);
        }
    }
    
    public int getPlayerCount() {
        return this.playerCount;
    }
    
    public void setPlayerCount(final int v) {
        this.playerCount = v;
    }
    
    public int getState() {
        return this.state;
    }
    
    @Override
    public String toString() {
        return "GameServer{template=" + this.template + ", serverName='" + this.serverName + '\'' + ", serverPort=" + this.serverPort + ", playerCount=" + this.playerCount + ", aliveChecks=" + this.aliveChecks + ", socket=" + this.socket + ", temp_path='" + "./templates/" + '\'' + ", servers_path='" + "./temp/" + '\'' + '}';
    }
}
