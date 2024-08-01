package de.cloud.plugin.bootstrap.jukebox.commands;

import de.cloud.api.CloudAPI;
import org.jetbrains.annotations.NotNull;
import org.jukeboxmc.api.command.Command;
import org.jukeboxmc.api.command.CommandSender;
import org.jukeboxmc.api.command.annotation.Description;
import org.jukeboxmc.api.command.annotation.Name;
import org.jukeboxmc.api.command.annotation.Permission;
import org.jukeboxmc.api.player.Player;

@Name("playerinfo")
@Permission("cloud.command.playerinfo")
@Description("Cloud player info command")
public class CloudPlayerInfoCommand implements Command {

    @Override
    public void execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            if (commandSender.hasPermission("cloud.command.playerinfo")) {
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
                    commandSender.sendMessage("Usage: /playerinfo <player>");
                }
            }
        }
    }
}
