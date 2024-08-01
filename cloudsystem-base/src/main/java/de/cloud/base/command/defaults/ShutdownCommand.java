package de.cloud.base.command.defaults;

import de.cloud.base.Base;
import de.cloud.base.command.CloudCommand;

@CloudCommand.Command(name = "stop", description = "Stops the cloudsystem", aliases = "exit")
public final class ShutdownCommand extends CloudCommand {

    @Override
    public void execute(Base base, String[] args) {
        base.onShutdown();
    }

}
