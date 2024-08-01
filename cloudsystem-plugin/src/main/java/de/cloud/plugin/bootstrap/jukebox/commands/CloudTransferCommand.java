package de.cloud.plugin.bootstrap.jukebox.commands;

import de.cloud.api.CloudAPI;
import org.jetbrains.annotations.NotNull;
import org.jukeboxmc.api.command.Command;
import org.jukeboxmc.api.command.CommandSender;
import org.jukeboxmc.api.command.annotation.Description;
import org.jukeboxmc.api.command.annotation.Name;
import org.jukeboxmc.api.command.annotation.Permission;
import org.jukeboxmc.api.player.Player;

@Name("transfer")
@Permission("cloud.network.command")
@Description("Cloud Transfer command")
public class CloudTransferCommand implements Command {

    @Override
    public void execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            if (commandSender.hasPermission("cloud.network.command")) {
                if (strings.length == 2) {
                    final var cloudPlayer = CloudAPI.getInstance().getPlayerManager().getCloudPlayer(strings[0]);
                    final var service = CloudAPI.getInstance().getServiceManager().getService(strings[1]);

                    if (cloudPlayer.isPresent()) {
                        if (service.isPresent()) {
                            if (cloudPlayer.get().getServer().equals(service.get())) {
                                cloudPlayer.get().sendMessage("§cYou are already connected to the service §e" + strings[1] + "§c.");
                                commandSender.sendMessage("§cThis player is already connected to the service §e" + strings[1] + "§c.");
                                return;
                            }

                            cloudPlayer.get().sendMessage("§aYou will be transferred to §e" + strings[1] + "§a.");
                            cloudPlayer.get().connect(service.get());
                        } else {
                            commandSender.sendMessage("§cThis service doesn't exists.");
                        }
                    } else {
                        commandSender.sendMessage("§cThis player doesn't exists.");
                    }
                } else {
                    commandSender.sendMessage("Usage: /transfer (player) (service)");
                }
            }
        }
    }
}
