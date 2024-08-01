package de.cloud.plugin.bootstrap.nukkit.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import de.cloud.wrapper.Wrapper;

public class NukkitListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Wrapper.getInstance().getPlayerManager().getCloudPlayer(player.getName()).isEmpty()) {
            player.kick("§cPlease join through the proxy", false);
        }
    }
}
