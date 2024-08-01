package de.cloud.plugin.bootstrap.waterdogpe.commands;

import de.cloud.plugin.bootstrap.global.CloudGlobalCommand;
import de.cloud.plugin.bootstrap.global.PlayerMessageObject;
import dev.waterdog.waterdogpe.command.Command;
import dev.waterdog.waterdogpe.command.CommandSender;
import dev.waterdog.waterdogpe.command.CommandSettings;
import org.jetbrains.annotations.NotNull;

public class WaterdogCloudCommand extends Command {

    public WaterdogCloudCommand() {
        super("cloud", CommandSettings.builder()
            .setDescription("Cloud command")
            .setPermission("cloud.network.command")
            .setUsageMessage("Usage: /cloud <list | shutdown | info>").build());
    }

    @Override
    public boolean onExecute(final CommandSender commandSender, final String alias, final String[] arguments) {
        CloudGlobalCommand.execute(new PlayerMessageObject() {
            @Override
            public void sendMessage(@NotNull String message) {
                commandSender.sendMessage(message);
            }

            @Override
            public boolean hasPermission(@NotNull String permissions) {
                return commandSender.hasPermission(permissions);
            }
        }, arguments);
        return true;
    }
}
