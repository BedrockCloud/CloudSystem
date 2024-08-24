package de.cloud.api.logger;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum LogType {

    SUCCESS("SUCCESS", "§a"),
    INFO("INFO", "§b"),
    ERROR("ERROR", "§c"),
    WARNING("WARNING", "§6"),
    EMPTY("", "");

    private final String name;
    private final String colorCode;

    @Override
    public String toString() {
        return String.format("%s%s§7", colorCode, name);
    }

    public String format(String message) {
        return String.format("%s: %s", this.toString(), message);
    }
}
