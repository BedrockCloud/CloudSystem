package de.cloud.base.command.defaults;

import de.cloud.base.Base;
import de.cloud.base.command.CloudCommand;
import de.cloud.modules.api.ILoadedModule;
import de.cloud.modules.api.annotation.Module;

import java.util.List;
import java.util.Map;

@CloudCommand.Command(name = "modules", description = "Modules command")
public class ModulesCommand extends CloudCommand {

    @Override
    public void execute(Base base, String[] args) {
        this.sendModuleList();
    }

    private void sendModuleList() {
        StringBuilder list = new StringBuilder();
        List<ILoadedModule> modules = Base.getInstance().getModuleProvider().getAllModules();
        for (ILoadedModule module : modules) {
            if (!list.isEmpty()) {
                list.append("§f, ");
            }
            list.append("§a");
            list.append(module.getDescription());
        }

        Base.getInstance().getLogger().log("§eCurrently are §f" + modules.size() + " §emodules loaded§f:\n" + list.toString());
    }
}
