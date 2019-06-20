package us.sparknetwork.base.command.chat;

import com.google.inject.Inject;
import me.fixeddev.bcm.parametric.CommandClass;

import me.fixeddev.bcm.parametric.annotation.Command;
import me.fixeddev.bcm.parametric.annotation.JoinedString;
import me.fixeddev.bcm.parametric.annotation.Parameter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.listeners.message.BroadcastListener;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.messages.BroadcastMessage;

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
    public boolean broadcastCommand(CommandSender sender, @JoinedString String message, @Parameter(value = "r", isFlag = true) boolean raw, @Parameter(value = "g", isFlag = true) boolean global) {
        if (global && sender.hasPermission("base.command.broadcast.global")) {
            BroadcastMessage.BroadcastType type = raw ? BroadcastMessage.BroadcastType.RAW : BroadcastMessage.BroadcastType.NORMAL;

            BroadcastMessage messageObject = new BroadcastMessage(type, message);

            broadcastChannel.sendMessage(messageObject);
        }
        if (raw) {
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message));
        } else {
            String broadcastMessage = ChatColor.translateAlternateColorCodes('&', MessageFormat.format(i18n.translate("broadcast.format"), message));

            Bukkit.broadcastMessage(broadcastMessage);
        }
        return true;
    }


}
