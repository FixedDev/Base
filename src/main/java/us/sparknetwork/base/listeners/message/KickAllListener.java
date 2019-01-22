package us.sparknetwork.base.listeners.message;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.messager.ChannelListener;
import us.sparknetwork.base.messager.messages.KickAllRequest;

public class KickAllListener implements ChannelListener<KickAllRequest> {
    @Inject
    private I18n i18n;

    @Override
    public void onMessageReceived(String channel, String serverSenderId, KickAllRequest data) {
        if (!channel.equals("kickAll")) {
            return;
        }

        Bukkit.getOnlinePlayers().stream()
                .filter(player -> !player.hasPermission(data.getBypassPermission()))
                .forEach(player -> player.kickPlayer(i18n.format("kick.message", data.getKicker(), data.getReason())));
    }
}
