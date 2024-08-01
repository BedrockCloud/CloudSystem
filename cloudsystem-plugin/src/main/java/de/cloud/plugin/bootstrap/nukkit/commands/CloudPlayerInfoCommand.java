package de.cloud.plugin.bootstrap.nukkit.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import de.cloud.api.CloudAPI;

public class CloudPlayerInfoCommand extends Command {
    public CloudPlayerInfoCommand() {
        super("playerinfo");
        this.setPermission("cloud.command.playerinfo");
        this.setUsage("Usage: /playerinfo <player>");
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender instanceof Player) {
            if (this.testPermission(commandSender)) {
                if (strings.length == 1) {
                    String playerName = strings[0];
                    if (CloudAPI.getInstance().getPlayerManager().getCloudPlayer(playerName).isEmpty()) {
                        commandSender.sendMessage("§cThis player is not online.");
                    } else {
                        final var cloudPlayer = CloudAPI.getInstance().getPlayerManager().getCloudPlayer(playerName).get();
                        commandSender.sendMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        commandSender.sendMessage("§7§l[ §ePlayer Info §7]");
                        commandSender.sendMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        commandSender.sendMessage("§fName: §e" + cloudPlayer.getUsername());
                        commandSender.sendMessage("§fServer: §e" + cloudPlayer.getServer().getName());
                        commandSender.sendMessage("§fProxy: §e" + cloudPlayer.getProxyServer().getName());
                        commandSender.sendMessage("§fAdresse: §e" + cloudPlayer.getAddress());
                        commandSender.sendMessage("§fUUID: §e" + cloudPlayer.getUniqueId().toString());
                        commandSender.sendMessage("§8━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    }
                } else {
                    commandSender.sendMessage(this.getUsage());
                }
            }
        }
        return false;
    }
}
