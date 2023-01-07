package com.bedrockcloud.bedrockcloud.command.defaults;

import com.bedrockcloud.bedrockcloud.BedrockCloud;
import com.bedrockcloud.bedrockcloud.command.CommandManager;
import com.bedrockcloud.bedrockcloud.console.Loggable;
import com.bedrockcloud.bedrockcloud.command.Command;

public class HelpCommand extends Command implements Loggable
{
    public HelpCommand() {
        super("help");
    }
    
    @Override
    protected void onCommand(final String[] args) {
        StringBuilder message = new StringBuilder("§aList of all commands:§r\n");
        for (Command cmd : BedrockCloud.commandManager.getCommands()){
            message.append("§7- §e").append(cmd.getCommand()).append("§r\n");
        }
        BedrockCloud.getLogger().command(message.toString());
    }
}
