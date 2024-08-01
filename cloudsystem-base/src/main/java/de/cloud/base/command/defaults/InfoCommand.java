package de.cloud.base.command.defaults;

import de.cloud.base.Base;
import de.cloud.base.command.CloudCommand;

@CloudCommand.Command(name = "info", description = "Prints information about the cloud")
public final class InfoCommand extends CloudCommand {

    @Override
    public void execute(Base base, String[] args) {
        var runtime = Runtime.getRuntime();
        var logger = base.getLogger();
        var version = Base.getInstance().getVersion();
        var nodeName = Base.getInstance().getNode().getName();
        var threadCount = Thread.getAllStackTraces().keySet().size();
        var usedMemory = calcMemory(runtime.totalMemory() - runtime.freeMemory());
        var maxMemory = calcMemory(runtime.maxMemory());

        logger.log(String.format("§7Version: §b%s§7", version),
            String.format("§7Node: §b%s§7", nodeName),
            String.format("§7Threads: §b%d§7", threadCount),
            String.format("§7RAM: §b%s/%s§7 mb", usedMemory, maxMemory));
    }

    private String calcMemory(long bytes) {
        return String.format("%.2f", bytes / (1024.0 * 1024.0));
    }

}
