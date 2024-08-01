package de.cloud.base.command;

import de.cloud.base.Base;
import de.cloud.api.CloudAPI;
import de.cloud.api.CloudAPIType;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Getter
public class SimpleCommandManager implements CommandManager {

    private final Map<String, CloudCommand> cachedCloudCommands = new HashMap<>();

    @Override
    public void execute(@NotNull String command) {
        List<String> args = new ArrayList<>(List.of(command.split(" ")));

        if (CloudAPI.getInstance().getCloudAPITypes() == CloudAPIType.NODE) {
            var commandName = args.removeFirst();
            var cloudCommand = cachedCloudCommands.get(commandName);
            if (cloudCommand != null) {
                cloudCommand.execute(Base.getInstance(), args.toArray(String[]::new));
            }
        }
    }

    @Override
    public void registerCommand(@NotNull CloudCommand command) {
        cachedCloudCommands.put(command.getName(), command);
        for (var alias : command.getAliases()) {
            cachedCloudCommands.put(alias, command);
        }
    }

    @Override
    public void registerCommands(@NotNull CloudCommand... commands) {
        for (var command : commands) {
            registerCommand(command);
        }
    }

    @Override
    public void unregisterCommand(@NotNull CloudCommand command) {
        var iterator = cachedCloudCommands.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().equals(command)) {
                iterator.remove();
            }
        }
    }
}
