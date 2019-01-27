package us.sparknetwork.base.listeners.message;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.handlers.server.ServerManager;
import us.sparknetwork.base.handlers.user.User;
import us.sparknetwork.base.handlers.user.UserHandler;
import us.sparknetwork.base.messager.ChannelListener;
import us.sparknetwork.base.messager.messages.StaffChatMessage;

import java.text.MessageFormat;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

public class StaffChatListener implements ChannelListener<StaffChatMessage> {

    @Inject
    private I18n i18n;

    @Inject
    private UserHandler settingsHandler;

    @Inject
    private ServerManager serverManager;

    @Override
    public void onMessageReceived(String channel, String serverId, StaffChatMessage data) {
        if (!channel.equals("staffChat")) {
            return;
        }

        addCallback(addOptionalToReturnValue(serverManager.findOne(serverId)), serverData -> {
            if (!serverData.isPresent()) {
                throw new IllegalStateException("Received helpop message with an invalid serverId");
            }

            String serverName = serverData.get().getId();

            String messageSender = data.getSenderNick();
            String chatMessage = data.getMessage();

            String staffFormattedMessage = MessageFormat.format(i18n.translate("chat.network.staff.format"), messageSender, chatMessage, serverName);

            Bukkit.getConsoleSender().sendMessage(staffFormattedMessage);

            Set<String> userIds = Bukkit.getOnlinePlayers().stream().filter(player -> (player.hasPermission("base.command.staffchat.see"))).map(Player::getUniqueId).map(UUID::toString).collect(Collectors.toSet());

            addCallback(settingsHandler.find(userIds, userIds.size()), userSettingsSet -> {
                userSettingsSet.stream().filter(User.ChatSettings::isStaffChatVisible).forEach(settings -> {
                    Player player = Bukkit.getPlayer(settings.getUUID());

                    if(player == null){
                        return;
                    }

                    player.sendMessage(staffFormattedMessage);
                });
            });

        });

    }
}
