package de.cloud.base.logger;

import de.cloud.base.console.SimpleConsoleManager;
import de.cloud.api.logger.LogType;
import de.cloud.api.logger.Logger;
import de.cloud.api.logger.LoggerAnsiFactory;
import lombok.Getter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jline.utils.InfoCmp;

import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SimpleLogger implements Logger {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH);

    @Getter
    private final SimpleConsoleManager consoleManager;

    @SneakyThrows
    public SimpleLogger() {
        this.consoleManager = new SimpleConsoleManager(this);

        System.setOut(new PrintStream(new LoggerOutputStream(this, LogType.INFO), true));
        System.setErr(new PrintStream(new LoggerOutputStream(this, LogType.ERROR), true));
    }

    @Override
    public String format(@NotNull String text, @NotNull LogType logType) {
        var message = "§r" + text + "§r";
        if (logType != LogType.EMPTY) {
            var currentTime = LocalTime.now().format(timeFormatter);
            var separator = this.isWindows() ? "|" : "┃";
            var indicator = this.isWindows() ? ">" : "»";
            message = String.format(" %s §7%s §r%s %s §r%s§r",
                currentTime, separator, logType.format(indicator), logType.getName(), message);

        }
        return LoggerAnsiFactory.toColorCode(message);
    }

    @Override
    public void log(@NotNull String text, @NotNull LogType logType) {
        var terminal = this.consoleManager.getTerminal();
        var coloredMessage = this.format(text, logType);
        terminal.puts(InfoCmp.Capability.carriage_return);
        terminal.writer().println(coloredMessage);
        terminal.flush();
        this.consoleManager.redraw();
    }

    @Override
    public void log(@NotNull String[] text, @NotNull LogType logType) {
        var terminal = this.consoleManager.getTerminal();
        terminal.puts(InfoCmp.Capability.carriage_return);
        for (var s : text) {
            var coloredMessage = this.format(s, logType);
            terminal.writer().println(coloredMessage);
        }
        terminal.flush();
        this.consoleManager.redraw();
    }

    @Override
    public void log(@NotNull String... text) {
        this.log(text, LogType.INFO);
    }

    public boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}
