package de.cloud.base.console;

import de.cloud.base.Base;
import de.cloud.api.logger.LogType;
import de.cloud.api.logger.Logger;
import org.jline.reader.LineReader;

public final class ConsoleReadingThread extends Thread {

    private final String consolePrompt;
    private final SimpleConsoleManager consoleManager;
    private final LineReader lineReader;

    public ConsoleReadingThread(final Logger logger, final SimpleConsoleManager consoleManager, final boolean windows) {
        super("Cloud-Console-Thread");
        this.consoleManager = consoleManager;
        this.lineReader = this.consoleManager.getLineReader();
        this.consolePrompt = logger.format("§6BedrockCloud §7" + (windows ? ">" : "»") + " §f", LogType.EMPTY);
    }

    @Override
    public void run() {
        try {
            while (!this.isInterrupted()) {
                var line = this.lineReader.readLine(this.consolePrompt);
                if (line != null && !line.isEmpty()) {
                    var input = this.consoleManager.getInputs().poll();
                    if (input != null) {
                        input.input().accept(line);
                    } else {
                        Base.getInstance().getCommandManager().execute(line);
                    }
                }
            }
        } catch (Exception e) {
            Base.getInstance().getLogger().log("Unexpected error in console thread: " + e.getMessage(), LogType.ERROR);
        }
    }
}
