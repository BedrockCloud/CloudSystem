package de.cloud.base.service;

import de.cloud.base.Base;
import de.cloud.base.config.editor.ConfigurationFileEditor;
import de.cloud.api.CloudAPI;
import de.cloud.api.groups.ServiceGroup;
import de.cloud.api.json.Document;
import de.cloud.api.logger.LogType;
import de.cloud.api.network.packet.ResponsePacket;
import de.cloud.api.network.packet.service.ServiceMemoryRequest;
import de.cloud.api.service.CloudService;
import de.cloud.api.service.ServiceState;
import de.cloud.api.version.GameServerVersion;
import de.cloud.network.packet.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.jar.JarFile;

@Getter
@Setter
public class LocalService implements CloudService {

    private final UUID uuid;
    private final ServiceGroup group;
    private final int serviceId;
    private final String node;
    private final int port;
    private final String hostName;

    private int maxPlayers;
    private String motd;
    private String state = ServiceState.PREPARED;
    private final File workingDirectory;
    private Process process;
    @Getter @Setter private boolean screen = false;
    private long startTime;

    public LocalService(final ServiceGroup group, final int id, final int port, final String hostname) {
        this.uuid = UUID.randomUUID();
        this.group = group;
        this.serviceId = id;
        this.node = Base.getInstance().getNode().getName();
        this.port = port;
        this.hostName = hostname;

        this.motd = this.group.getMotd();
        this.maxPlayers = this.group.getDefaultMaxPlayers();

        this.workingDirectory = new File(
            this.group.isStatic() ? "static/" + getName() : "tmp/" + getName() + "." + this.uuid
        );
    }

    @SneakyThrows
    public void start() {
        setState(ServiceState.STARTING);
        downloadVersion(group.getGameServerVersion());

        startTime = System.currentTimeMillis();
        workingDirectory.mkdirs();
        Base.getInstance().getGroupTemplateService().copyTemplates(this);

        var storageFolder = new File("storage/jars");
        var jarFile = new File(storageFolder, group.getGameServerVersion().getJar());
        Files.copy(jarFile.toPath(), new File(workingDirectory, jarFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);

        setupPlugin();
        writeServiceProperties();
        configureServerFiles();

        process = new ProcessBuilder(arguments())
            .directory(workingDirectory)
            .start();
    }

    private void setupPlugin() throws IOException {
        var pluginDirectory = new File(workingDirectory, "plugins");
        pluginDirectory.mkdir();
        Files.copy(
            ((SimpleServiceManager) Base.getInstance().getServiceManager()).getPluginPath(),
            new File(pluginDirectory, "plugin.jar").toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
    }

    private void writeServiceProperties() throws IOException {
        new Document()
            .set("service", getName())
            .set("node", node)
            .set("hostname", Base.getInstance().getNode().getHostName())
            .set("port", Base.getInstance().getNode().getPort())
            .write(new File(workingDirectory, "property.json"));
    }

    private void configureServerFiles() throws IOException {
        if (group.getGameServerVersion() == GameServerVersion.WATERDOG) {
            configureWaterdog();
        } else if (group.getGameServerVersion() == GameServerVersion.NUKKIT){
            configureNukkit();
        } else if (group.getGameServerVersion() == GameServerVersion.JUKEBOXMC) {
            configureJukebox();
        }
    }

    private void configureWaterdog() throws IOException {
        var configFilePath = Path.of(workingDirectory.getPath(), "config.yml");

        if (Files.notExists(configFilePath)) {
            try (var inputStream = getClass().getClassLoader().getResourceAsStream("defaultFiles/config.yml")) {
                if (inputStream == null) {
                    throw new IOException("Default config file not found");
                }
                Files.copy(inputStream, configFilePath);
            }
        }

        new ConfigurationFileEditor(configFilePath, line -> line.startsWith("host: ") ? "host: \"0.0.0.0:" + port + "\"" : line);
    }


    private void configureNukkit() throws IOException {
        var nukkitYAML = new File(workingDirectory, "nukkit.yml");
        if (!nukkitYAML.exists()) {
            try (var inputStream = getClass().getClassLoader().getResourceAsStream("defaultFiles/nukkit.yml")) {
                assert inputStream != null;
                FileUtils.copyToFile(inputStream, nukkitYAML);
            }
        }

        var propertiesFile = new File(workingDirectory, "server.properties");
        var properties = new Properties();

        if (propertiesFile.exists()) {
            try (var fileReader = new FileReader(propertiesFile)) {
                properties.load(fileReader);
            }
        } else {
            try (var inputStreamReader = new InputStreamReader(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("defaultFiles/server.properties"))
            )) {
                properties.load(inputStreamReader);
            }
        }
        properties.setProperty("server-name", getName());
        properties.setProperty("server-port", String.valueOf(port));
        try (var fileWriter = new FileWriter(propertiesFile)) {
            properties.store(fileWriter, null);
        }
    }

    private void configureJukebox() throws IOException {
        var configFilePath = Path.of(workingDirectory.getPath(), "properties.json");

        if (Files.notExists(configFilePath)) {
            try (var inputStream = getClass().getClassLoader().getResourceAsStream("defaultFiles/properties.json")) {
                if (inputStream == null) {
                    throw new IOException("Default properties file not found");
                }
                Files.copy(inputStream, configFilePath);
            }
        }

        new ConfigurationFileEditor(configFilePath, line -> line.startsWith("\"address:\"") ? "\"host:\" \"0.0.0.0\"" : line);
        new ConfigurationFileEditor(configFilePath, line -> line.startsWith("\"port:\"") ? "\"port:\" " + Double.parseDouble(String.valueOf(port)) : line);
    }

    @Override
    public @NotNull String getName() {
        return group.getName() + "-" + serviceId;
    }

    @Override
    public void edit(@NotNull Consumer<CloudService> serviceConsumer) {
        serviceConsumer.accept(this);
        update();
    }

    public void update() {
        CloudAPI.getInstance().getServiceManager().updateService(this);
    }

    @Override
    public void sendPacket(@NotNull Packet packet) {
        CloudAPI.getInstance().getServiceManager().sendPacketToService(this, packet);
    }

    @Override
    public void executeCommand(@NotNull String command) {
        if (process != null) {
            OutputStream outputStream = null;
            try {
                outputStream = process.getOutputStream();
                if (outputStream != null) {
                    outputStream.write((command + "\n").getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                }
            } catch (IOException ignored) {
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    public void stop() {
        stopProcess();
        delete();
    }

    private void stopProcess() {
        if (process != null) {
            executeCommand(group.getGameServerVersion().isProxy() ? "end" : "stop");
            try {
                if (process.waitFor(5, TimeUnit.SECONDS)) {
                    process = null;
                    return;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            process.destroyForcibly();
            process = null;
        }
    }

    private void delete() {
        if (group.isStatic()) return;
        synchronized (this) {
            try {
                FileUtils.deleteDirectory(workingDirectory);
            } catch (IOException e) {
                System.err.println("Failed to delete directory: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    private List<String> arguments() {
        var base = Base.getInstance();
        var arguments = new ArrayList<>(Arrays.asList(
            base.getConfig().getJavaCommand(),
            "-XX:+UseG1GC",
            "-XX:+ParallelRefProcEnabled",
            "-XX:MaxGCPauseMillis=200",
            "-XX:+UnlockExperimentalVMOptions",
            "-XX:+DisableExplicitGC",
            "-XX:+AlwaysPreTouch",
            "-XX:G1NewSizePercent=30",
            "-XX:G1MaxNewSizePercent=40",
            "-XX:G1HeapRegionSize=8M",
            "-XX:G1ReservePercent=20",
            "-XX:G1HeapWastePercent=5",
            "-XX:G1MixedGCCountTarget=4",
            "-XX:InitiatingHeapOccupancyPercent=15",
            "-XX:G1MixedGCLiveThresholdPercent=90",
            "-XX:G1RSetUpdatingPauseTimePercent=5",
            "-XX:SurvivorRatio=32",
            "-XX:+PerfDisableSharedMem",
            "-XX:MaxTenuringThreshold=1",
            "-Dusing.aikars.flags=https://mcflags.emc.gs",
            "-Daikars.new.flags=true",
            "-XX:-UseAdaptiveSizePolicy",
            "-XX:CompileThreshold=100",
            "-Dio.netty.recycler.maxCapacity=0",
            "-Dio.netty.recycler.maxCapacity.default=0",
            "-Djline.terminal=jline.UnsupportedTerminal",
            "-Dfile.encoding=UTF-8",
            "-Dclient.encoding.override=UTF-8",
            "-DIReallyKnowWhatIAmDoingISwear=true",
            "-Xms" + group.getMaxMemory() + "M",
            "-Xmx" + group.getMaxMemory() + "M"
        ));

        arguments.addAll(base.getConfig().getJvmFlags());

        var serviceManager = (SimpleServiceManager) base.getServiceManager();
        var applicationFile = new File(workingDirectory, group.getGameServerVersion().getJar());

        arguments.add("-cp");
        arguments.add(serviceManager.getWrapperPath().toString());
        arguments.add("-javaagent:" + serviceManager.getWrapperPath());
        arguments.add(serviceManager.getWrapperMainClass());

        boolean preLoadClasses = false;

        try (var jarFile = new JarFile(applicationFile)) {
            arguments.add(jarFile.getManifest().getMainAttributes().getValue("Main-Class"));
            preLoadClasses = jarFile.getEntry("META-INF/versions.list") != null;
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        arguments.add(applicationFile.getAbsolutePath());
        arguments.add(Boolean.toString(preLoadClasses));
        return arguments;
    }

    private void downloadVersion(GameServerVersion gameServerVersion) {
        var directory = new File("storage/jars");
        var file = new File(directory, gameServerVersion.getJar());

        if (file.exists()) return;

        CloudAPI.getInstance().getLogger().log("§7Downloading §bVersion§7... (§3" + gameServerVersion.getName() + "§7)");

        directory.mkdirs();
        try {
            URI uri = new URI(gameServerVersion.getUrl());
            URL url = uri.toURL();

            FileUtils.copyURLToFile(url, file);
            CloudAPI.getInstance().getLogger().log("§7Downloading of (§3" + getName() + "§7)§a successfully §7completed.");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            CloudAPI.getInstance().getLogger().log("§cFailed to download version§7... (§3" + getName() + "§7)", LogType.ERROR);
        }
    }

    public CompletableFuture<Integer> getMemory() {
        var result = new CompletableFuture<Integer>();
        sendPacket(new ResponsePacket(new ServiceMemoryRequest(), packet ->
            result.complete(((ServiceMemoryRequest) packet).getMemory())
        ));
        return result;
    }
}
