package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.listeners.message.HelpopListener;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.messages.HelpopMessage;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

public class HelpopCommands implements CommandClass {

    private Channel<HelpopMessage> helpopChannel;

    @Inject
    private I18n i18n;

    @Inject
    public HelpopCommands(Messenger messager, HelpopListener listener) {
        helpopChannel = messager.getChannel("helpop", HelpopMessage.class);
        helpopChannel.registerListener(listener);
    }

    @Command(names = {"helpop", "ac", "request"}, min = 1, permission = "base.command.helpop", usage = "Usage: /<command> <text...>")
    public boolean helpopCommand(CommandSender sender, CommandContext context) {
        String message = context.getJoinedArgs(0);

        String helpopMessage = MessageFormat.format(i18n.translate("helpop"), sender.getName(), message);

        Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("base.command.helpop.receive")).forEach(player -> {
            player.sendMessage(helpopMessage);
        });

        Bukkit.getConsoleSender().sendMessage(helpopMessage);

        HelpopMessage globalHelpopMessage = new HelpopMessage(sender.getName(), message);
        helpopChannel.sendMessage(globalHelpopMessage);

        sender.sendMessage(i18n.translate("helpop.send"));

        return true;
    }
}
