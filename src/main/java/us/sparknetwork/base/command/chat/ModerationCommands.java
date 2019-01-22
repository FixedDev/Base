

package us.sparknetwork.base.command.chat;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.ServerConfigurations;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;
import us.sparknetwork.utils.DateUtil;

import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ModerationCommands implements CommandClass {

    @Inject
    private I18n i18n;

    @Command(names = {"clearchat", "cc"}, usage = "Usage: /<command> [reason]", flags = {'s'}, permission = "base.command.clearchat")
    public boolean clearChat(CommandSender sender, CommandContext context) {
        String reason = context.getArguments().size() > 0 ? context.getJoinedArgs(0) : "None";

        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
            IntStream.range(0, 100).forEach(number -> onlinePlayer.sendMessage("\n\n\n\n\n\n\n\n\n\n"));
        });

        if (!context.hasFlag('s')) {
            if (!(sender instanceof Player)) {
                Bukkit.broadcastMessage(MessageFormat.format(i18n.translate("cleared.chat"), sender.getName(), reason));

                return true;
            }
            Player playerSender = (Player) sender;

            Bukkit.broadcastMessage(MessageFormat.format(i18n.translate("cleared.chat"), playerSender.getDisplayName(), reason));
        }
        return true;
    }

    @Command(names = {"mutechat", "mc"}, usage = "Usage: /<command>", max = 1, permission = "base.command.mutechat")
    public boolean muteChat(CommandSender sender, CommandContext context) {
        long time = TimeUnit.MINUTES.toMillis(5);

        if (context.getArguments().size() == 1) {
            long newTime = DateUtil.parse(context.getArgument(0));
            time = newTime == -1 ? TimeUnit.MINUTES.toMillis(5) : newTime;
        }

        if (ServerConfigurations.MUTED_CHAT > System.currentTimeMillis()) {
            ServerConfigurations.MUTED_CHAT = 0;
            ServerConfigurations.getInstance().saveConfig();

            Bukkit.broadcastMessage(MessageFormat.format(i18n.translate("chat.unmuted"), sender.getName()));
            return true;
        }

        ServerConfigurations.MUTED_CHAT = System.currentTimeMillis() + time;

        ServerConfigurations.getInstance().saveConfig();

        if (!(sender instanceof Player)) {
            Bukkit.broadcastMessage(MessageFormat.format(i18n.translate("muted.chat"), sender.getName(), DateUtil.getHumanReadableDate(time)));

            return true;
        }
        Player playerSender = (Player) sender;

        Bukkit.broadcastMessage(MessageFormat.format(i18n.translate("muted.chat"), playerSender.getDisplayName(), DateUtil.getHumanReadableDate(time)));
        return true;
    }

    @Command(names = {"slowchat"}, usage = "Usage: /<command>", max = 1, permission = "base.command.slowchat")
    public boolean slowChat(CommandSender sender, CommandContext context) {

        long time = TimeUnit.MINUTES.toMillis(5);

        if (context.getArguments().size() == 1) {
            long newTime = DateUtil.parse(context.getArgument(0));
            time = newTime == -1 ? TimeUnit.MINUTES.toMillis(5) : newTime;
        }

        if (ServerConfigurations.SLOW_CHAT > System.currentTimeMillis()) {
            ServerConfigurations.SLOW_CHAT = 0;
            ServerConfigurations.getInstance().saveConfig();

            Bukkit.broadcastMessage(MessageFormat.format(i18n.translate("chat.unslowed"), sender.getName()));
            return true;
        }

        ServerConfigurations.SLOW_CHAT = System.currentTimeMillis() + time;

        ServerConfigurations.getInstance().saveConfig();

        if (!(sender instanceof Player)) {
            Bukkit.broadcastMessage(MessageFormat.format(i18n.translate("slowed.chat"), sender.getName(), DateUtil.getHumanReadableDate(time)));

            return true;
        }
        Player playerSender = (Player) sender;

        Bukkit.broadcastMessage(MessageFormat.format(i18n.translate("slowed.chat"), playerSender.getDisplayName(), DateUtil.getHumanReadableDate(time)));
        return true;
    }
}

