package com.bedrockcloud.bedrockcloud.server.privategameserver;

import com.bedrockcloud.bedrockcloud.BedrockCloud;
import com.bedrockcloud.bedrockcloud.network.DataPacket;
import com.bedrockcloud.bedrockcloud.network.packets.GameServerDisconnectPacket;
import com.bedrockcloud.bedrockcloud.server.port.PortValidator;
import com.bedrockcloud.bedrockcloud.templates.Template;
import com.bedrockcloud.bedrockcloud.api.MessageAPI;

import java.io.*;
import java.net.*;
import java.util.concurrent.ThreadLocalRandom;

public class PrivateGameServer
{
    private final Template template;
    private final String serverName;
    private final int serverPort;
    public int pid;
    public int state;
    private int playerCount;
    private int aliveChecks;
    private DatagramSocket socket;
    public final String temp_path = "./templates/";
    public final String servers_path = "./temp/";
    public String serverOwner = null;

    public PrivateGameServer(final Template template, String serverOwner) {
        this.template = template;
        this.aliveChecks = 0;
        this.serverName = template.getName() + "-" + BedrockCloud.getGameServerProvider().getFreeNumber("./temp/" + template.getName());
        this.serverPort = PortValidator.getNextPrivateServerPort(this);
        this.playerCount = 0;
        this.state = 0;
        this.pid = -1;
        BedrockCloud.getPrivateGameServerProvider().addGameServer(this);
        this.copyServer();
        this.serverOwner = serverOwner;
        try {
            this.startServer();
        } catch (InterruptedException e) {
            BedrockCloud.getLogger().exception(e);
        }
    }
    
    public int getPid() {
        return this.pid;
    }
    
    public String getServerName() {
        return this.serverName;
    }

    public String getServerOwner(){
        return this.serverOwner;
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
            BedrockCloud.getLogger().warning(notifyMessage);
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
            BedrockCloud.getLogger().warning(notifyMessage);
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
        BedrockCloud.getPrivateGameServerProvider().createProperties(this);
    }
    
    public Template getTemplate() {
        return this.template;
    }
    
    public void stopServer() {
        String notifyMessage = MessageAPI.stopMessage.replace("%service", this.serverName);
        BedrockCloud.sendNotifyCloud(notifyMessage);
        BedrockCloud.getLogger().warning(notifyMessage);

        final GameServerDisconnectPacket packet = new GameServerDisconnectPacket();
        packet.addValue("reason", "Server Stopped");
        this.pushPacket(packet);
    }

    public void killWithPID() throws IOException {
        String notifyMessage = MessageAPI.stoppedMessage.replace("%service", this.serverName);
        BedrockCloud.sendNotifyCloud(notifyMessage);
        BedrockCloud.getLogger().warning(notifyMessage);

        final ProcessBuilder builder = new ProcessBuilder();
        try {
            builder.command("/bin/sh", "-c", "screen -X -S " + this.serverName + " kill").start();
        } catch (Exception ignored) {
        }
        try {
            builder.command("/bin/sh", "-c", "kill " + this.pid).start();
        } catch (Exception ignored) {
        }

        try {
            BedrockCloud.getGameServerProvider().deleteServer(new File("./temp/" + this.serverName), this.serverName);
        } catch (NullPointerException ex) {
            BedrockCloud.getLogger().exception(ex);
        }

        this.getTemplate().removeServer(this.getServerName());
        BedrockCloud.getPrivateGameServerProvider().removeServer(getServerName());
    }

    public DatagramSocket getSocket() {
        return this.socket;
    }

    public void setSocket(final DatagramSocket socket) {
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
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            byteArrayOutputStream.write(cloudPacket.encode().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] data = byteArrayOutputStream.toByteArray();
        InetAddress address = null;
        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ignored) {
        }
        int port = getServerPort()+1;
        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, address, port);
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException ex) {
            BedrockCloud.getLogger().exception(ex);
        }
        try {
            datagramSocket.send(datagramPacket);
        } catch (IOException ex) {
            BedrockCloud.getLogger().exception(ex);
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
        return "PrivateGameServer{template=" + this.template + ", serverName='" + this.serverName + '\'' + ", serverPort=" + this.serverPort + ", playerCount=" + this.playerCount + ", aliveChecks=" + this.aliveChecks + ", socket=" + this.socket + ", temp_path='" + "./templates/" + '\'' + ", servers_path='" + "./temp/" + '\'' + '}';
    }
}
