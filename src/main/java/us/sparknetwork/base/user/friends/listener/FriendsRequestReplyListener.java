package us.sparknetwork.base.user.friends.listener;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.messager.ChannelListener;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.user.friends.FriendRequestReply;

public class FriendsRequestReplyListener implements ChannelListener<FriendRequestReply> {
    @Inject
    private JavaPlugin plugin;
    @Inject
    private I18n i18n;
    @Inject
    private UserHandler userHandler;
    @Inject
    private ListeningExecutorService executorService;

    @Override
    public void onMessageReceived(String channel, String serverSenderId, FriendRequestReply data) {
        if (channel.equalsIgnoreCase("friendRequestReply")) {
            return;
        }

        if (data.getRequest() == null) {
            plugin.getLogger().warning("Received a FriendRequestReply with empty FriendRequest from server " + serverSenderId + ", maybe an error occurred ?)");
            return;
        }

        OfflinePlayer offlineRequestSender = Bukkit.getOfflinePlayer(data.getRequest().getTo());

        if (!offlineRequestSender.isOnline()) {
            return;
        }

        Player requestSender = offlineRequestSender.getPlayer();

        executorService.submit(() -> {
            User.Complete to = userHandler.findOneSync(data.getRequest().getTo().toString());

            if(to == null){
                return;
            }

            switch (data.getRequestReply()) {
                case DENIED:
                    requestSender.getPlayer().sendMessage(i18n.format("friends.request.denied", to.hasNick() ? to.getNick() : to.getLastName()));
                    break;
                case ACCEPTED:
                    requestSender.getPlayer().sendMessage(i18n.format("friends.request.accepted", to.hasNick() ? to.getNick() : to.getLastName()));
                    break;
                case NOT_FOUND:
                default:
                    plugin.getLogger().severe("Received a FriendRequestReply with a FriendRequest, but it's marked with an invalid reply.");
                    break;
            }
        });
    }
}
