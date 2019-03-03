package us.sparknetwork.base.command.chat;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.listeners.message.BroadcastListener;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.messages.BroadcastMessage;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

public class BroadcastCommand implements CommandClass {

    @Inject
    private I18n i18n;

    private Channel<BroadcastMessage> broadcastChannel;

    @Inject
    public BroadcastCommand(Messenger messager, BroadcastListener listener) {
        broadcastChannel = messager.getChannel("broadcast", BroadcastMessage.class);
        broadcastChannel.registerListener(listener);
    }

    @Command(names = {"broadcast", "bc", "bcraw", "bcr", "broadcastraw"},
            min = 1,
            usage = "Usage: /<command> <message...> [-r] [-g]",
            permission = "base.command.broadcast",
            flags = {'r', 'g'})
    public boolean broadcastCommand(CommandSender sender, CommandContext context) {
        if (context.getJoinedArgs(0).trim().isEmpty()) {
            return false;
        }

        if (context.hasFlag('g') && sender.hasPermission("base.command.broadcast.global")) {
            BroadcastMessage.BroadcastType type = context.hasFlag('r') ? BroadcastMessage.BroadcastType.RAW : BroadcastMessage.BroadcastType.NORMAL;

            BroadcastMessage message = new BroadcastMessage(type, context.getJoinedArgs(0));

            broadcastChannel.sendMessage(message);
        }
        if (context.getNoValueFlags().contains('r')) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', context.getJoinedArgs(0)));
        } else {
            String broadcastMessage = ChatColor.translateAlternateColorCodes('&', MessageFormat.format(i18n.translate("broadcast.format"), context.getJoinedArgs(0)));

            Bukkit.broadcastMessage(broadcastMessage);
        }
        return true;
    }


}
