package com.bedrockcloud.bedrockcloud.server.proxy;

import com.bedrockcloud.bedrockcloud.BedrockCloud;
import com.bedrockcloud.bedrockcloud.config.Config;
import com.bedrockcloud.bedrockcloud.network.DataPacket;
import com.bedrockcloud.bedrockcloud.server.port.PortValidator;
import com.bedrockcloud.bedrockcloud.templates.Template;
import com.bedrockcloud.bedrockcloud.api.MessageAPI;

import java.io.*;

import com.bedrockcloud.bedrockcloud.network.packets.ProxyServerDisconnectPacket;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class ProxyServer
{
    private final Template template;
    private final String serverName;
    private int serverPort;
    private Socket socket;
    public final String temp_path = "./templates/";
    public final String servers_path = "./temp/";
    public int pid;
    
    public ProxyServer(final Template template) {
        this.template = template;
        this.serverName = template.getName() + "-" + BedrockCloud.getGameServerProvider().getFreeNumber("./temp/" + template.getName());
        this.serverPort = PortValidator.getNextProxyServerPort(this);
        this.pid = -1;
        BedrockCloud.getProxyServerProvider().addProxyServer(this);
        this.copyServer();
        try {
            this.startServer();
        }
        catch (InterruptedException e) {
            BedrockCloud.getLogger().exception(e);
        }
    }
    
    public Template getTemplate() {
        return this.template;
    }
    
    public String getServerName() {
        return this.serverName;
    }

    public Socket getSocket() {
        return this.socket;
    }

    public void setSocket(final Socket socket) {
        this.socket = socket;
    }
    
    public int getServerPort() {
        return this.serverPort;
    }
    
    private static boolean isAvailablePort(final int port) {
        Socket s = null;
        try {
            s = new Socket("localhost", port);
            return false;
        }
        catch (IOException e2) {
            return true;
        }
        finally {
            if (s != null) {
                try {
                    s.close();
                } catch (IOException e) {
                    BedrockCloud.getLogger().exception(e);
                }
            }
        }
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
                builder.command("/bin/sh", "-c", "screen -dmS " + this.serverName + " java -jar WaterdogPE.jar").directory(new File("./temp/" + this.serverName)).start();
            } catch (Exception e) {
                BedrockCloud.getLogger().exception(e);
            }
        } else {
            String notifyMessage = MessageAPI.startFailed.replace("%service", serverName);
            BedrockCloud.sendNotifyCloud(notifyMessage);
            BedrockCloud.getLogger().warning(notifyMessage);
        }
    }
    
    public void stopServer() {
        String notifyMessage = MessageAPI.stopMessage.replace("%service", this.serverName);
        BedrockCloud.sendNotifyCloud(notifyMessage);
        BedrockCloud.getLogger().warning(notifyMessage);

        final ProxyServerDisconnectPacket packet = new ProxyServerDisconnectPacket();
        packet.addValue("reason", "Proxy Stopped");
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
    
    public void copyServer() {
        final File src = new File("./templates/" + this.template.getName() + "/");
        final File dest = new File("./temp/" + this.serverName);
        BedrockCloud.getGameServerProvider().copy(src, dest);
        final File global_plugins = new File("./local/plugins/waterdogpe");
        final File dest_plugs = new File("./temp/" + this.serverName + "/plugins/");
        BedrockCloud.getGameServerProvider().copy(global_plugins, dest_plugs);
        final File file = new File("./local/versions/waterdogpe");
        final File dest_lib = new File("./temp/" + this.serverName + "/");
        BedrockCloud.getGameServerProvider().copy(file, dest_lib);
        final Config config = new Config("./temp/" + this.serverName + "/cloud.yml", 2);
        config.set("name", this.serverName);
        config.save();
        final Config proxy = new Config("./temp/" + this.serverName + "/config.yml", 2);
        proxy.set("listener.host", "0.0.0.0:" + this.getServerPort());
        proxy.set("listener.max_players", this.getTemplate().getMaxPlayers());
        if (this.getTemplate().getMaintenance()) {
            proxy.set("listener.motd", "§c§oMaintenance");
        } else {
            proxy.set("listener.motd", BedrockCloud.getConfig().getString("motd"));
        }
        proxy.set("use_login_extras", BedrockCloud.getConfig().get("wdpe-login-extras"));
        proxy.set("replace_username_spaces", true);
        proxy.set("cloud-path", BedrockCloud.getCloudPath());
        proxy.save();
    }
}
