package us.sparknetwork.base.listeners.message;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.server.ServerManager;
import us.sparknetwork.base.messager.ChannelListener;
import us.sparknetwork.base.messager.messages.HelpopMessage;

import java.text.MessageFormat;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

public class HelpopListener implements ChannelListener<HelpopMessage> {
    @Inject
    private I18n i18n;

    @Inject
    private ServerManager serverManager;

    @Override
    public void onMessageReceived(String channel, String serverId, HelpopMessage data) {
        if (!channel.equals("helpop")) {
            return;
        }

        addCallback(addOptionalToReturnValue(serverManager.findOne(serverId)), optionalServer -> {
            if (!optionalServer.isPresent()) {
                throw new IllegalStateException("Received helpop message with an invalid serverId");
            }

            String serverName = optionalServer.get().getId();

            String helpMessage = data.getHelpMessage();
            String senderNick = data.getSenderNick();

            String helpopMessage = MessageFormat.format(i18n.translate("helpop.network"), senderNick, serverName, helpMessage);

            Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("base.command.helpop.receive")).forEach(player -> {
                player.sendMessage(helpopMessage);
            });

            Bukkit.getConsoleSender().sendMessage(helpopMessage);
        });


    }
}
