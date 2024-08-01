package de.cloud.plugin.bootstrap.global;

import org.jetbrains.annotations.NotNull;

public interface PlayerMessageObject {

    void sendMessage(@NotNull String message);

    boolean hasPermission(@NotNull String permission);

}
