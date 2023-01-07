package com.bedrockcloud.bedrockcloud;

import com.bedrockcloud.bedrockcloud.command.defaults.*;
import com.bedrockcloud.bedrockcloud.console.shutdown.ShutdownThread;
import com.bedrockcloud.bedrockcloud.network.packets.*;
import com.bedrockcloud.bedrockcloud.player.CloudPlayerProvider;
import com.bedrockcloud.bedrockcloud.config.Config;
import com.bedrockcloud.bedrockcloud.server.privategameserver.PrivateGameServer;
import com.bedrockcloud.bedrockcloud.server.privategameserver.PrivateGameServerProvider;
import com.bedrockcloud.bedrockcloud.tasks.KeepALiveTask;
import com.bedrockcloud.bedrockcloud.tasks.PrivateKeepALiveTask;
import com.bedrockcloud.bedrockcloud.tasks.RestartAllTask;
import com.bedrockcloud.bedrockcloud.templates.Template;
import com.bedrockcloud.bedrockcloud.files.json.json;
import com.bedrockcloud.bedrockcloud.api.GroupAPI;
import com.bedrockcloud.bedrockcloud.network.DataPacket;
import com.bedrockcloud.bedrockcloud.server.proxy.ProxyServer;
import com.bedrockcloud.bedrockcloud.server.gameserver.GameServer;
import com.bedrockcloud.bedrockcloud.command.CommandManager;
import com.bedrockcloud.bedrockcloud.network.NetworkManager;
import com.bedrockcloud.bedrockcloud.network.handler.PacketHandler;
import com.bedrockcloud.bedrockcloud.server.proxy.ProxyServerProvider;
import com.bedrockcloud.bedrockcloud.server.gameserver.GameServerProvider;
import com.bedrockcloud.bedrockcloud.templates.TemplateProvider;
import com.bedrockcloud.bedrockcloud.console.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.net.Socket;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;

@VersionInfo(name = "BedrockCloud", version = "2.0.0", developers = { "BedrockCloud" }, identifier = "@Stable")
public class BedrockCloud
{
    public static TemplateProvider templateProvider;
    public static GameServerProvider gameServerProvider;
    public static PrivateGameServerProvider privategameServerProvider;
    public static ProxyServerProvider proxyServerProvider;
    public static CloudPlayerProvider cloudPlayerProvider;
    public static PacketHandler packetHandler;
    public static NetworkManager networkManager;
    public static CommandManager commandManager;
    private static Socket socket;
    protected static boolean running;

    public final static String prefix = "§l§bCloud §r§8» §r";
    
    public static Logger getLogger() {
        return new Logger();
    }
    
    public static Socket getSocket() {
        return BedrockCloud.socket;
    }
    
    public static void setSocket(final Socket sockets) {
        BedrockCloud.socket = sockets;
    }

    public BedrockCloud() {

        Thread.currentThread().setName("BedrockCloud");

        running = true;
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());

        System.setProperty("java.net.preferIPv4Stack" , "true");

        int javaVersion = getJavaVersion();
        if (javaVersion < 17) {
            getLogger().error("Using unsupported Java version! Minimum supported version is Java 17, found Java " + javaVersion);
            return;
        }

        Config config = getConfig();

        config.set("version", Objects.requireNonNull(getVersion()).version());
        config.save();

        this.initProvider();
        this.registerCommands();
        getLogger().info("This cloud was developed by §b" + Arrays.toString(Objects.requireNonNull(getVersion()).developers()).replace("[", "").replace("]", ""));
        getLogger().info("Version of cloud - §b" + getVersion().version() + getVersion().identifier());
        BedrockCloud.networkManager = new NetworkManager((int) getConfig().getDouble("port"));
        getTemplateProvider().onLoad();

        final Timer ttime = new Timer();
        if (getConfig().getBoolean("auto-restart-cloud", false)) {
            ttime.schedule(new RestartAllTask(), 1000L, 1000L);
        }

        ttime.schedule(new KeepALiveTask(), 1000L, 1000L);
        ttime.schedule(new PrivateKeepALiveTask(), 1000L, 1000L);

        this.startAllProxies();
        BedrockCloud.networkManager.start();
    }

    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        BedrockCloud.running = running;
    }
    
    private void initProvider() {
        BedrockCloud.commandManager = new CommandManager();
        BedrockCloud.templateProvider = new TemplateProvider();
        BedrockCloud.gameServerProvider = new GameServerProvider();
        BedrockCloud.privategameServerProvider = new PrivateGameServerProvider();
        BedrockCloud.proxyServerProvider = new ProxyServerProvider();
        BedrockCloud.cloudPlayerProvider = new CloudPlayerProvider();
        BedrockCloud.packetHandler = new PacketHandler();
        registerPackets();
        BedrockCloud.commandManager.start();
    }
    
    private static void registerPackets() {
        getPacketHandler().registerPacket("GameServerConnectPacket", GameServerConnectPacket.class);
        getPacketHandler().registerPacket("GameServerDisconnectPacket", GameServerDisconnectPacket.class);
        getPacketHandler().registerPacket("ProxyServerConnectPacket", ProxyServerConnectPacket.class);
        getPacketHandler().registerPacket("ProxyServerDisconnectPacket", ProxyServerDisconnectPacket.class);
        getPacketHandler().registerPacket("GameServerInfoRequestPacket", GameServerInfoRequestPacket.class);
        getPacketHandler().registerPacket("GameServerInfoResponsePacket", GameServerInfoResponsePacket.class);
        getPacketHandler().registerPacket("ListServerRequestPacket", ListServerRequestPacket.class);
        getPacketHandler().registerPacket("ListServerResponsePacket", ListServerResponsePacket.class);
        getPacketHandler().registerPacket("ListCloudPlayersRequestPacket", ListCloudPlayersRequestPacket.class);
        getPacketHandler().registerPacket("ListCloudPlayersResponsePacket", ListCloudPlayersResponsePacket.class);
        getPacketHandler().registerPacket("PlayerMessagePacket", PlayerMessagePacket.class);
        getPacketHandler().registerPacket("ProxyPlayerJoinPacket", ProxyPlayerJoinPacket.class);
        getPacketHandler().registerPacket("ProxyPlayerQuitPacket", ProxyPlayerQuitPacket.class);
        getPacketHandler().registerPacket("UnregisterServerPacket", UnregisterServerPacket.class);
        getPacketHandler().registerPacket("RegisterServerPacket", RegisterServerPacket.class);
        getPacketHandler().registerPacket("KeepALivePacket", KeepALivePacket.class);
        getPacketHandler().registerPacket("UpdateGameServerInfoPacket", UpdateGameServerInfoPacket.class);
        getPacketHandler().registerPacket("StartGroupPacket", StartGroupPacket.class);
        getPacketHandler().registerPacket("StartServerPacket", StartServerPacket.class);
        getPacketHandler().registerPacket("StartPrivateServerPacket", StartPrivateServerPacket.class);
        getPacketHandler().registerPacket("StopGroupPacket", StopGroupPacket.class);
        getPacketHandler().registerPacket("StopServerPacket", StopServerPacket.class);
        getPacketHandler().registerPacket("StopPrivateServerPacket", StopPrivateServerPacket.class);
        getPacketHandler().registerPacket("CloudPlayerAddPermissionPacket", CloudPlayerAddPermissionPacket.class);
        getPacketHandler().registerPacket("VersionInfoPacket", VersionInfoPacket.class);
        getPacketHandler().registerPacket("CloudNotifyMessagePacket", CloudNotifyMessagePacket.class);
        getPacketHandler().registerPacket("PlayerMovePacket", PlayerMovePacket.class);
        getPacketHandler().registerPacket("NodeServerConnectPacket", NodeServerConnectPacket.class);
        getPacketHandler().registerPacket("NodeServerMessagePacket", NodeServerMessagePacket.class);
        getPacketHandler().registerPacket("PlayerKickPacket", PlayerKickPacket.class);
        getPacketHandler().registerPacket("CloudPlayerChangeServerPacket", CloudPlayerChangeServerPacket.class);
        getPacketHandler().registerPacket("SendToHubPacket", SendToHubPacket.class);
        getPacketHandler().registerPacket("ListTemplatesRequestPacket", ListTemplatesRequestPacket.class);
        getPacketHandler().registerPacket("ListTemplatesResponsePacket", ListTemplatesResponsePacket.class);
    }

    @SuppressWarnings("unchecked")
    private void startAllProxies() {
        try {
            Thread.sleep(3000L);
        }
        catch (InterruptedException e) {
            getLogger().exception(e);
        }
        getLogger().info("Waiting for ProxyConnection...");
        for (final String name : GroupAPI.getGroups()) {
            try {
                final HashMap<String, Object> stats = (HashMap<String, Object>)json.get(name, 9);
                if (Integer.parseInt(stats.get("type").toString()) == 0) {
                    final Template group = getTemplateProvider().getTemplate(name);
                    if (group != null) {
                        if (!getTemplateProvider().isTemplateRunning(group)) {
                            group.start(false);
                        }
                    }
                }
            } catch (IOException e2) {
                getLogger().exception(e2);
            }
        }
    }
    
    public static void sendNotifyCloud(final String message) {
        for (final ProxyServer proxy : getProxyServerProvider().getProxyServerMap().values()) {
            final CloudNotifyMessagePacket packet = new CloudNotifyMessagePacket();
            packet.message = prefix + message;
            proxy.pushPacket(packet);
        }
    }
    
    private void registerCommands() {
        BedrockCloud.commandManager.addCommand(new HelpCommand());
        BedrockCloud.commandManager.addCommand(new ServerCommand());
        BedrockCloud.commandManager.addCommand(new TemplateCommand());
        BedrockCloud.commandManager.addCommand(new SoftwareCommand());
        BedrockCloud.commandManager.addCommand(new PlayerCommand());
        BedrockCloud.commandManager.addCommand(new EndCommand());
        BedrockCloud.commandManager.addCommand(new InfoCommand());
    }
    
    public static VersionInfo getVersion() {
        return BedrockCloud.class.isAnnotationPresent(VersionInfo.class) ? BedrockCloud.class.getAnnotation(VersionInfo.class) : null;
    }
    
    public static TemplateProvider getTemplateProvider() {
        return BedrockCloud.templateProvider;
    }
    
    public static ProxyServerProvider getProxyServerProvider() {
        return BedrockCloud.proxyServerProvider;
    }
    
    public static Config getConfig() {
        return new Config("./local/config.json", 1);
    }

    public static Config getTemplateConfig() {
        return new Config("./templates/config.json", 1);
    }
    
    public static PacketHandler getPacketHandler() {
        return BedrockCloud.packetHandler;
    }
    
    public static GameServerProvider getGameServerProvider() {
        return BedrockCloud.gameServerProvider;
    }

    public static PrivateGameServerProvider getPrivateGameServerProvider() {
        return BedrockCloud.privategameServerProvider;
    }
    
    public static CloudPlayerProvider getCloudPlayerProvider() {
        return BedrockCloud.cloudPlayerProvider;
    }

    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    public static long getUsedMemory() {
        MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        return heapMemoryUsage.getUsed();
    }

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public static String getCloudPath(){
        try {
            String path = BedrockCloud.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();
            String fullPath = path.substring(path.lastIndexOf("/") + 1);
            return path.replace(fullPath, "");
        } catch (NullPointerException | URISyntaxException e){
            BedrockCloud.getLogger().exception(e);
            return "";
        }
    }

    public static void printCloudInfos() {
        BedrockCloud.getLogger().command("Used Memory   :  " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) + " bytes");
        BedrockCloud.getLogger().command("Free Memory   : " + Runtime.getRuntime().freeMemory() + " bytes");
        BedrockCloud.getLogger().command("Total Memory  : " + Runtime.getRuntime().totalMemory() + " bytes");
        BedrockCloud.getLogger().command("Max Memory    : " + Runtime.getRuntime().maxMemory() + " bytes");
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2, 3));
        }

        int index = version.indexOf(".");
        if (index != -1) {
            version = version.substring(0, index);
        }
        return Integer.parseInt(version);
    }

    public static void shutdown(){
        try {
            if (BedrockCloud.networkManager.serverSocket != null && !BedrockCloud.networkManager.serverSocket.isClosed()){
                BedrockCloud.networkManager.serverSocket.close();
                BedrockCloud.getLogger().warning("§cServerSocket was closed.");
            }
        } catch (IOException ignored) {}

        for (final String templateName : BedrockCloud.getTemplateProvider().templateMap.keySet()) {
            BedrockCloud.getTemplateProvider().removeRunningGroup(templateName);
        }

        for (final GameServer gameServer : BedrockCloud.getGameServerProvider().gameServerMap.values()) {
            gameServer.stopServer();
            try {
                gameServer.killWithPID();
            } catch (IOException ignored) {}
        }

        for (final PrivateGameServer gameServer : BedrockCloud.getPrivateGameServerProvider().gameServerMap.values()) {
            gameServer.stopServer();
            try {
                gameServer.killWithPID();
            } catch (IOException ignored) {}
        }

        for (final String proxy : BedrockCloud.getProxyServerProvider().proxyServerMap.keySet()) {
            final ProxyServer proxyServer = BedrockCloud.getProxyServerProvider().getProxyServer(proxy);
            proxyServer.stopServer();
            try {
                proxyServer.killWithPID();
            } catch (IOException ignored) {}
        }
        try {
            BedrockCloud.getLogger().info("§cPlease wait until the cloud has been stopped.");
            Thread.sleep(3000L);
        } catch (Exception ignored){}
    }
}