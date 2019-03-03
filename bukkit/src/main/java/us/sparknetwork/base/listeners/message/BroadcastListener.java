package us.sparknetwork.base.listeners.message;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.messager.ChannelListener;
import us.sparknetwork.base.messager.messages.BroadcastMessage;

import java.text.MessageFormat;
import java.util.UUID;

public class BroadcastListener implements ChannelListener<BroadcastMessage> {

    @Inject
    private I18n i18n;

    @Override
    public void onMessageReceived(String channel, String serverId, BroadcastMessage data) {
        if (!channel.equals("broadcast")) {
            return;
        }

        String broadcastMessage = data.getMessage();
        BroadcastMessage.BroadcastType type = data.getType();

        switch (type) {
            case RAW:
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage));
                break;
            default:
            case NORMAL:
                Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', MessageFormat.format(i18n.translate("broadcast.format"), broadcastMessage)));
                break;

        }
    }
}
