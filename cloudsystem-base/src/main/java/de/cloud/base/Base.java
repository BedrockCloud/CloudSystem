package de.cloud.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.cloud.RestBootstrap;
import de.cloud.base.command.defaults.*;
import de.cloud.base.config.CloudConfiguration;
import de.cloud.base.group.SimpleGroupManager;
import de.cloud.base.module.ModuleProvider;
import de.cloud.base.node.BaseNode;
import de.cloud.api.CloudAPI;
import de.cloud.api.CloudAPIType;
import de.cloud.api.groups.GroupManager;
import de.cloud.api.json.Document;
import de.cloud.api.logger.LogType;
import de.cloud.api.logger.Logger;
import de.cloud.api.player.PlayerManager;
import de.cloud.api.service.ServiceManager;
import de.cloud.base.command.CommandManager;
import de.cloud.base.command.SimpleCommandManager;
import de.cloud.base.console.SimpleConsoleManager;
import de.cloud.base.dependencies.Dependency;
import de.cloud.base.dependencies.DependencyHandler;
import de.cloud.base.logger.SimpleLogger;
import de.cloud.base.player.SimplePlayerManager;
import de.cloud.base.service.LocalService;
import de.cloud.base.service.SimpleServiceManager;
import de.cloud.base.templates.GroupTemplateService;
import de.cloud.database.DatabaseManager;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Getter
public final class Base extends CloudAPI {

    @Getter
    private static Base instance;

    private final String version;
    private CloudConfiguration config;
    private final DependencyHandler dependencyHandler;

    @Getter
    private CommandManager commandManager;
    private BaseNode node;
    private DatabaseManager databaseManager;
    private GroupManager groupManager;
    private ServiceManager serviceManager;
    private PlayerManager playerManager;
    private GroupTemplateService groupTemplateService;
    @Getter
    private WorkerThread workerThread;
    @Getter
    private boolean running = true;
    @Getter
    private final ModuleProvider moduleProvider;

    private static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/";
    private static final String OWNER = "BedrockCloud";
    private static final String REPO = "CloudSystem";

    public Base(final DependencyHandler dependencyHandler) {
        super(CloudAPIType.NODE);
        instance = this;
        this.dependencyHandler = dependencyHandler;
        this.version = getClass().getPackage().getImplementationVersion();
        this.logger = new SimpleLogger();
        this.moduleProvider = new ModuleProvider();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            getLogger().log(
                "Unexpected exception in thread " + t.getName() + ": " + e.getClass().getSimpleName() + " - " + e.getMessage(),
                LogType.ERROR
            );
            e.printStackTrace();
        });

        var configFile = new File("config.json");
        if (loadConfig(configFile)) {
            logger.log("§cPlease configure your database in the '§b" + configFile.getName() + "§7'!", LogType.WARNING);
            return;
        }
        checkVersion();

        dependencyHandler.loadDependency(switch (config.getDatabaseConfiguration().getDatabaseType()) {
            case MYSQL -> Dependency.MYSQL_DRIVER;
            case MONGODB -> Dependency.MONGO_DRIVER;
            case H2 -> Dependency.H2_DATABASE;
        });

        commandManager = new SimpleCommandManager();
        databaseManager = DatabaseManager.newInstance(config.getDatabaseConfiguration());
        workerThread = new WorkerThread(this);
        groupManager = new SimpleGroupManager();
        serviceManager = new SimpleServiceManager();
        groupTemplateService = new GroupTemplateService();
        playerManager = new SimplePlayerManager();
        node = new BaseNode(config);

        // Register commands
        commandManager.registerCommands(
            new ClearCommand(),
            new GroupCommand(),
            new HelpCommand(),
            new InfoCommand(),
            new ServiceCommand(),
            new ShutdownCommand(),
            new ScreenCommand()
        );

        // Add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown, "Cloud-Shutdown-Thread"));

        // Print success message
        logger.log("               ", LogType.EMPTY);
        logger.log("§7The cloud was successfully started.", LogType.SUCCESS);
        logger.log("               ", LogType.EMPTY);

        ((SimpleLogger) logger).getConsoleManager().start();

        workerThread.start();
        new RestBootstrap(config.getRestPassword());
        this.getModuleProvider().loadModules();
    }

    public static void main() {
        var dependencyHandler = new DependencyHandler();
        dependencyHandler.loadDependencies(
            Dependency.GSON,
            Dependency.JLINE,
            Dependency.JANSI,
            Dependency.COMMONS_IO,
            Dependency.NETTY_CODEC,
            Dependency.NETTY_TRANSPORT_EPOLL,
            Dependency.NETTY_TRANSPORT,
            Dependency.NETTY_BUFFER,
            Dependency.NETTY_COMMON,
            Dependency.NETTY_RESOLVER,
            Dependency.NETTY_UNIX_COMMON
        );
        new Base(dependencyHandler);
    }

    private boolean loadConfig(@NotNull File file) {
        if (file.exists()) {
            config = new Document(file).get(CloudConfiguration.class);
            return false;
        }
        new Document(config = new CloudConfiguration()).write(file);
        return true;
    }

    private void checkVersion() {
        if (config.isCheckForUpdate() && !version.endsWith("SNAPSHOT")) {
            String link = GITHUB_API_URL + OWNER + "/" + REPO + "/tags";

            try {
                URI uri = new URI(link);
                URL url = uri.toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (var in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                        var response = new StringBuilder();
                        String inputLine;

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }

                        var tagsArray = JsonParser.parseString(response.toString()).getAsJsonArray();
                        var versions = new ArrayList<String>();

                        for (JsonElement tagElement : tagsArray) {
                            var tagName = tagElement.getAsJsonObject().get("name").getAsString();
                            versions.add(tagName);
                        }

                        var currentVersion = getVersion();
                        if (versions.isEmpty() || versions.contains(currentVersion)) {
                            getLogger().log("You are using the latest version of the Cloud.", LogType.INFO);
                        } else {
                            var latestVersion = getLatestVersion(versions);
                            getLogger().log("An update is available: version " + latestVersion, LogType.WARNING);
                        }
                    }
                } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    getLogger().log("Repository not found. Please check the owner and repository name.", LogType.ERROR);
                } else {
                    getLogger().log("GitHub API responded with status code " + responseCode, LogType.ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getLatestVersion(List<String> versions) {
        versions.sort(Base::compareVersions);
        return versions.getLast();
    }

    public static int compareVersions(String v1, String v2) {
        var parts1 = v1.split("\\.");
        var parts2 = v2.split("\\.");

        int length = Math.max(parts1.length, parts2.length);
        for (int i = 0; i < length; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (part1 < part2) return -1;
            if (part1 > part2) return 1;
        }
        return 0;
    }

    public void onShutdown() {
        if (!running) return;
        running = false;
        logger.log("§7Trying to terminate the §bcloudsystem§7.");
        ((SimpleLogger) logger).getConsoleManager().shutdownReading();
        serviceManager.getAllCachedServices().stream()
            .filter(service -> service instanceof LocalService)
            .map(service -> (LocalService) service)
            .forEach(LocalService::stop);

        try {
            // Delete wrapper and plugin jars
            Files.deleteIfExists(((SimpleServiceManager) serviceManager).getWrapperPath());
            Files.deleteIfExists(((SimpleServiceManager) serviceManager).getPluginPath());

            // Delete temporary directory
            FileUtils.deleteDirectory(new File("tmp"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        node.close();
        databaseManager.close();
        logger.log("§aSuccessfully §7stopped the §bcloudsystem§7.", LogType.SUCCESS);
        System.exit(0);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    public SimpleConsoleManager getConsoleManager() {
        return ((SimpleLogger) logger).getConsoleManager();
    }

    public String generateRandomPassword(int length) {
        var random = new SecureRandom();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALLOWED_CHARS.length());
            sb.append(ALLOWED_CHARS.charAt(randomIndex));
        }
        return sb.toString();
    }
}
