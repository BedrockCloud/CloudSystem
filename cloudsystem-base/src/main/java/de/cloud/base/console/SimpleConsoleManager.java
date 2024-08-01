package de.cloud.base.console;

import de.cloud.api.logger.Logger;
import de.cloud.base.logger.SimpleLogger;
import lombok.Getter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public final class SimpleConsoleManager {

    private final Logger logger;
    private Thread consoleReadingThread;
    @Getter
    private final Terminal terminal;
    @Getter
    private final LineReader lineReader;
    private final boolean windowsSystem;

    @Getter
    private final Queue<ConsoleInput> inputs;

    public SimpleConsoleManager(Logger logger) throws IOException {
        this.logger = logger;
        this.windowsSystem = ((SimpleLogger) logger).isWindows();

        this.terminal = TerminalBuilder.builder()
            .system(true)
            .streams(System.in, System.out)
            .encoding(StandardCharsets.UTF_8)
            .dumb(true)
            .build();

        this.lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
            .option(LineReader.Option.AUTO_REMOVE_SLASH, false)
            .option(LineReader.Option.INSERT_TAB, false)
            .completer(new ConsoleCompleter(this))
            .build();

        clearConsole();
        this.inputs = new LinkedList<>();
    }

    public void start() {
        this.consoleReadingThread = new ConsoleReadingThread(logger, this, windowsSystem);
        this.consoleReadingThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
        this.consoleReadingThread.start();
    }

    public void clearConsole() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
        redraw();
    }

    public void redraw() {
        if (lineReader.isReading()) {
            lineReader.callWidget(LineReader.REDRAW_LINE);
            lineReader.callWidget(LineReader.REDISPLAY);
        }
    }

    public void shutdown() throws IOException {
        terminal.close();
    }

    public void shutdownReading() {
        if (consoleReadingThread != null && consoleReadingThread.isAlive()) {
            consoleReadingThread.interrupt();
        }
    }

    public void addInput(Consumer<String> input, List<String> tabCompletions) {
        inputs.add(new ConsoleInput(input, tabCompletions));
    }
}
