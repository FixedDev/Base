package us.sparknetwork.base.listeners;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import us.sparknetwork.base.I18n;

public class JoinFullServer implements Listener {

    @Inject
    private I18n i18n;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.KICK_FULL) {
            return;
        }
        if (e.getPlayer().hasPermission("base.joinfull")) {
            e.setResult(PlayerLoginEvent.Result.ALLOWED);
            return;
        }

        e.setResult(PlayerLoginEvent.Result.KICK_FULL);
        e.setKickMessage(i18n.translate("server.full"));
    }
}