package us.sparknetwork.base.user.friends.listener;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.api.messager.ChannelListener;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.user.friends.FriendRequest;

public class FriendRequestListener implements ChannelListener<FriendRequest> {
    @Inject
    private I18n i18n;
    @Inject
    private JavaPlugin plugin;
    @Inject
    private ListeningExecutorService executorService;
    @Inject
    private UserHandler userHandler;

    @Override
    public void onMessageReceived(String channel, String serverSenderId, FriendRequest data) {
        if (!channel.equalsIgnoreCase("friendRequest")) {
            return;
        }

        if (data == null) {
            plugin.getLogger().warning("Received an empty FriendRequest from server " + serverSenderId + ", maybe an error occurred ?)");
            return;
        }

        OfflinePlayer offlineRequestReceiver = Bukkit.getOfflinePlayer(data.getTo());

        if (!offlineRequestReceiver.isOnline()) {
            return;
        }

        Player requestReceiver = offlineRequestReceiver.getPlayer();

        executorService.submit(() -> {
            User.Complete to = userHandler.findOneSync(data.getTo().toString());

            if (to == null) {
                plugin.getLogger().warning("Received an FriendRequest with an invalid sender, requestId: " + data.getId() + " senderId: " + data.getTo().toString());
                return;
            }

            requestReceiver.getPlayer().sendMessage(i18n.format("friends.request.receive", to.hasNick() ? to.getNick() : to.getLastName()));
        });

    }
}
