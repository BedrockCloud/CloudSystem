package de.cloud.base.command.defaults;

import de.cloud.base.Base;
import de.cloud.base.command.CloudCommand;

@CloudCommand.Command(name = "clear", description = "Clears the console")
public final class ClearCommand extends CloudCommand {

    @Override
    public void execute(Base base, String[] args) {
        base.getConsoleManager().clearConsole();
    }

}
