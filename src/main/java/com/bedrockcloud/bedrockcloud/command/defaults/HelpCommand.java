package com.bedrockcloud.bedrockcloud.command.defaults;

import com.bedrockcloud.bedrockcloud.console.Loggable;
import com.bedrockcloud.bedrockcloud.command.Command;

public class HelpCommand extends Command implements Loggable
{
    public HelpCommand() {
        super("help");
    }
    
    @Override
    protected void onCommand(final String[] args) {
     this.getLogger().info("§a--Commands-- §r\n\n§7- end §eStops the Cloud§r\n§7- help §eShows you all commands§r\n§7- info §eShows you informations about the System§r\n§7- player §eShows infos about Players§r\n§7- server §eManage your Servers§r\n§7- software §eUpdate Versions§r\n§7- template §eManage your Templates§r");
    }
}
