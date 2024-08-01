package de.cloud.base.logger;

import de.cloud.api.logger.LogType;
import de.cloud.api.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public final class LoggerOutputStream extends ByteArrayOutputStream {

    private final Logger logger;
    private final LogType logType;

    public LoggerOutputStream(Logger logger, LogType logType) {
        this.logger = logger;
        this.logType = logType;
    }

    @Override
    public void flush() {
        String input = toString(StandardCharsets.UTF_8);
        reset();
        if (input != null && !input.isEmpty()) {
            logger.log(input.split("\n"), logType);
        }
    }
}
