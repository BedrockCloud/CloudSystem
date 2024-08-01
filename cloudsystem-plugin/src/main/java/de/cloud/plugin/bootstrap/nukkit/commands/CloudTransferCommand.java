package de.cloud.plugin.bootstrap.nukkit.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import de.cloud.api.CloudAPI;

public class CloudTransferCommand extends Command {

    public CloudTransferCommand() {
        super("transfer", "Cloud transfer command");
        this.setPermission("cloud.network.command");
        this.setUsage("Usage: /transfer (player) (service)");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (!(commandSender instanceof Player) || !this.testPermission(commandSender)) {
            return false;
        }

        if (strings.length == 2) {
            final var cloudPlayer = CloudAPI.getInstance().getPlayerManager().getCloudPlayer(strings[0]);
            final var service = CloudAPI.getInstance().getServiceManager().getService(strings[1]);
            if (cloudPlayer.isPresent()) {
                if (service.isPresent()) {
                    if (cloudPlayer.get().getServer().equals(service.get())) {
                        cloudPlayer.get().sendMessage("§cYou are already connected to the service §e" + strings[1] + "§c.");
                        commandSender.sendMessage("§cThis player is already connected to the service §e" + strings[1] + "§c.");
                        return false;
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
            commandSender.sendMessage(this.getUsage());
        }
        return false;
    }
}
