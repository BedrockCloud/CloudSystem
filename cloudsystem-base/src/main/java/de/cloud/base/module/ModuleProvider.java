package de.cloud.base.module;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.cloud.api.logger.LogType;
import de.cloud.base.Base;
import de.cloud.modules.api.ILoadedModule;
import de.cloud.modules.api.IModule;
import de.cloud.modules.api.IModuleProvider;
import lombok.SneakyThrows;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * ModuleProvider manages the loading, unloading, and reloading of modules.
 * It keeps a cache of loaded modules and provides methods to interact with them.
 *
 * @author TeriumCloud
 */
public class ModuleProvider implements IModuleProvider {

    private final Map<String, ILoadedModule> loadedModuleCache = new HashMap<>();

    public void loadModules() {
        Path moduleDir = Path.of("modules");
        if (!Files.exists(moduleDir)) {
            try {
                Files.createDirectories(moduleDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (var files = Files.list(moduleDir)) {
            files.filter(file -> file.toString().endsWith(".jar"))
                .forEach(module -> loadModule(module.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public void executeModule(File file, String mainClass, String method) {
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()})) {
            Class<?> moduleClass = Class.forName(mainClass, true, classLoader);
            IModule cloudModule = (IModule) moduleClass.getDeclaredConstructor().newInstance();

            if ("enable".equals(method)) {
                cloudModule.onEnable();
            } else {
                cloudModule.onDisable();
            }
        } catch (ReflectiveOperationException | IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void loadModule(String path) {
        try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(Path.of(path)))) {
            JarEntry entry;
            while ((entry = jarInputStream.getNextJarEntry()) != null) {
                if ("module-info.json".equals(entry.getName())) {
                    try (Reader reader = new InputStreamReader(jarInputStream, StandardCharsets.UTF_8)) {
                        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                        String moduleName = jsonObject.get("name").getAsString();

                        if (!loadedModuleCache.containsKey(moduleName)) {
                            ILoadedModule module = createLoadedModule(jsonObject, path);
                            loadedModuleCache.put(moduleName, module);

                            if (System.getProperty("module-reloading") == null) {
                                Base.getInstance().getLogger().log(
                                    String.format("Loaded module '§b%s§f' by '§b%s§f' v%s.",
                                        moduleName,
                                        module.getAuthor(),
                                        module.getVersion()
                                    ), LogType.INFO
                                );
                            }
                            executeModule(new File(path), module.getMainClass(), "enable");
                            return;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ILoadedModule createLoadedModule(JsonObject jsonObject, String path) {
        return new ILoadedModule() {
            @Override
            public String getName() {
                return jsonObject.get("name").getAsString();
            }

            @Override
            public String getFileName() {
                return Path.of(path).getFileName().toString();
            }

            @Override
            public String getAuthor() {
                return jsonObject.get("author").getAsString();
            }

            @Override
            public String getVersion() {
                return jsonObject.get("version").getAsString();
            }

            @Override
            public String getDescription() {
                return jsonObject.get("description").getAsString();
            }

            @Override
            public String getMainClass() {
                return jsonObject.get("main-class").getAsString();
            }

            @Override
            public boolean isReloadable() {
                return jsonObject.get("reloadable").getAsBoolean();
            }
        };
    }

    public void unloadModule(ILoadedModule module) {
        loadedModuleCache.remove(module.getName());
        executeModule(new File("modules", module.getFileName()), module.getMainClass(), "disable");
    }

    public void reloadModule(ILoadedModule module) {
        System.setProperty("module-reloading", "true");
        unloadModule(module);
        loadModule(Path.of("modules", module.getFileName()).toString());
        System.clearProperty("module-reloading");
        Base.getInstance().getLogger().log(String.format("Successfully reloaded module '§b%s§f'.", module.getName()), LogType.INFO);
    }

    @Override
    public Optional<ILoadedModule> getModuleByName(String name) {
        return Optional.ofNullable(loadedModuleCache.get(name));
    }

    @Override
    public List<ILoadedModule> getAllModules() {
        return new ArrayList<>(loadedModuleCache.values());
    }
}
