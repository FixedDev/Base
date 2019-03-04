package us.sparknetwork.base.command.essentials;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.listeners.message.KickAllListener;
import us.sparknetwork.base.api.messager.Channel;
import us.sparknetwork.base.api.messager.Messenger;
import us.sparknetwork.base.messager.messages.KickAllRequest;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

public class KickAllCommand implements CommandClass {

    private static final String DEFAULT_BYPASS_PERMISSION = "base.kickall.bypass";

    @Inject
    private I18n i18n;

    private Channel<KickAllRequest> messageChannel;

    @Inject
    KickAllCommand(Messenger messenger, KickAllListener listener){
        messageChannel = messenger.getChannel("kickAll", KickAllRequest.class);
        messageChannel.registerListener(listener);
    }

    @Command(names = "kickall", permission = "base.command.kickall", flags = 'g', usage = "Usage: /<command> [bypassPermission] [reason]")
    public boolean kickAll(CommandSender sender, CommandContext context) {

        String bypassPermission = DEFAULT_BYPASS_PERMISSION;

        if(context.getArguments().size() >= 1){
            bypassPermission = context.getArgument(0);
        }

        String kickReason = i18n.translate("kick.reason.default");

        if(context.getArguments().size() > 1){
            kickReason = context.getJoinedArgs(1);
        }

        if(context.hasFlag('g') && sender instanceof ConsoleCommandSender){
            KickAllRequest kickAllRequest = new KickAllRequest(sender.getName(), kickReason, bypassPermission);

            messageChannel.sendMessage(kickAllRequest);

            sender.sendMessage(i18n.format("kickall.global", bypassPermission, kickReason));
            return true;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if(player.hasPermission(bypassPermission)){
               continue;
            }

            player.kickPlayer(i18n.format("kick.message", sender.getName(), kickReason));
        }

        sender.sendMessage(i18n.format("kickall", bypassPermission, kickReason));
        return true;
    }
}
