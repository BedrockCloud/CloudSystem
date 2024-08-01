package de.cloud.modules.api;

import java.util.List;
import java.util.Optional;

/**
 * @author TeriumCloud
 */
public interface IModuleProvider {

    /**
     * load a module by path
     */
    void loadModule(String path);

    /**
     * Get a module by name
     *
     * @return ILoadedModule This returns the module by name.
     */
    Optional<ILoadedModule> getModuleByName(String name);

    /**
     * Get all loaded modules
     *
     * @return List<ILoadedModule> This returns a list of all loaded modules.
     */
    List<ILoadedModule> getAllModules();
}
