package de.cloud.wrapper;

import de.cloud.api.CloudAPI;
import de.cloud.api.CloudAPIType;
import de.cloud.api.groups.GroupManager;
import de.cloud.api.json.Document;
import de.cloud.api.logger.Logger;
import de.cloud.api.network.packet.ResponsePacket;
import de.cloud.api.network.packet.init.CacheInitPacket;
import de.cloud.api.network.packet.service.ServiceMemoryRequest;
import de.cloud.api.player.PlayerManager;
import de.cloud.api.service.CloudService;
import de.cloud.api.service.ServiceManager;
import de.cloud.wrapper.group.WrapperGroupManager;
import de.cloud.wrapper.loader.ApplicationExternalClassLoader;
import de.cloud.wrapper.logger.WrapperLogger;
import de.cloud.wrapper.network.WrapperClient;
import de.cloud.wrapper.player.CloudPlayerManager;
import de.cloud.wrapper.service.WrapperServiceManager;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public final class Wrapper extends CloudAPI {

    private static Instrumentation instrumentation;

    public static void premain(final String agentArgs, final Instrumentation instrumentation) {
        Wrapper.instrumentation = instrumentation;
    }

    public static void main(String[] args) {
        try {
            var wrapper = new Wrapper();
            var cacheInitialized = new AtomicBoolean(false);

            wrapper.getPacketHandler().registerPacketListener(CacheInitPacket.class, (ctx, packet) -> cacheInitialized.set(true));

            var arguments = new ArrayList<>(List.of(args));
            var mainClassName = arguments.remove(0);
            var applicationFile = Path.of(arguments.remove(0));
            var useExternalClassLoader = Boolean.parseBoolean(arguments.remove(0));

            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            if (useExternalClassLoader) {
                classLoader = new ApplicationExternalClassLoader().addUrl(applicationFile);
                try (var jarInputStream = new JarInputStream(Files.newInputStream(applicationFile))) {
                    JarEntry jarEntry;
                    while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                        if (jarEntry.getName().endsWith(".class")) {
                            Class.forName(jarEntry.getName().replace('/', '.').replace(".class", ""), false, classLoader);
                        }
                    }
                }
            }

            instrumentation.appendToSystemClassLoaderSearch(new JarFile(applicationFile.toFile()));
            var mainClass = Class.forName(mainClassName, true, classLoader);
            var thread = new Thread(() -> {
                try {
                    mainClass.getMethod("main", String[].class).invoke(null, (Object) arguments.toArray(new String[0]));
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }, "Cloud-Service-Thread");

            thread.setContextClassLoader(classLoader);
            if (cacheInitialized.get()) {
                thread.start();
            } else {
                wrapper.getPacketHandler().registerPacketListener(CacheInitPacket.class, (ctx, packet) -> thread.start());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Getter
    private static Wrapper instance;

    private final GroupManager groupManager;
    private final ServiceManager serviceManager;
    private final PlayerManager playerManager;
    @Getter
    private final WrapperClient client;

    public Wrapper() {
        super(CloudAPIType.SERVICE);
        instance = this;

        var property = new Document(new File("property.json")).get(PropertyFile.class);

        this.logger = new WrapperLogger();
        this.groupManager = new WrapperGroupManager();
        this.serviceManager = new WrapperServiceManager(property);
        this.playerManager = new CloudPlayerManager();
        this.client = new WrapperClient(this.packetHandler, property.getService(), property.getHostname(), property.getPort());

        Runtime.getRuntime().addShutdownHook(new Thread(this::stop, "Cloud-Shutdown-Thread"));

        packetHandler.registerPacketListener(ResponsePacket.class, (ctx, packet) -> {
            if (packet.getPacket() instanceof ServiceMemoryRequest memoryRequest) {
                memoryRequest.setMemory((int) calcMemory(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
            }
            ctx.channel().writeAndFlush(packet);
        });
    }

    private void stop() {
        this.client.close();
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public @NotNull GroupManager getGroupManager() {
        return this.groupManager;
    }

    @Override
    public @NotNull ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    @Override
    public @NotNull PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public CloudService thisService() {
        return ((WrapperServiceManager) this.serviceManager).thisService();
    }

    private long calcMemory(final long memory) {
        return memory / 1024 / 1024;
    }
}
