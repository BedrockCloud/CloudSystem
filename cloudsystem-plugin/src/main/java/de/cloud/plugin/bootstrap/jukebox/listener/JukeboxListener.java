package de.cloud.plugin.bootstrap.jukebox.listener;

import de.cloud.wrapper.Wrapper;
import org.jukeboxmc.api.event.EventHandler;
import org.jukeboxmc.api.event.Listener;
import org.jukeboxmc.api.event.player.PlayerJoinEvent;
import org.jukeboxmc.api.player.Player;

public class JukeboxListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Wrapper.getInstance().getPlayerManager().getCloudPlayer(player.getName()).isEmpty()) {
            player.kick("§cPlease join through the proxy", false);
        }
    }
}
