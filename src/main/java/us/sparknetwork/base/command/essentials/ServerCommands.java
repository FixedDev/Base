package us.sparknetwork.base.command.essentials;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.handlers.server.LocalServerData;
import us.sparknetwork.base.handlers.server.Server;
import us.sparknetwork.base.handlers.server.ServerManager;
import us.sparknetwork.base.handlers.user.finder.UserFinder;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;
import us.sparknetwork.utils.ListenableFutureUtils;

import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

public class ServerCommands implements CommandClass {

    @Inject
    private I18n i18n;
    @Inject
    private LocalServerData serverData;
    @Inject
    private ServerManager serverManager;
    @Inject
    private UserFinder userFinder;

    @Command(names = {"findUser"}, usage = "Usage: /<command> <player>", permission = "base.command.finduser", min = 1, max = 1)
    public boolean findUser(CommandSender sender, CommandContext context) {
        ListenableFutureUtils.addCallback(userFinder.findUser(context.getArgument(0), UserFinder.Scope.GLOBAL), server -> {
            if (server == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), context.getArgument(0)));
                return;
            }
            sender.sendMessage(MessageFormat.format(i18n.translate("user.get.server"), context.getArgument(0), server.getId(), server.getDisplayName()));

        });
        return true;
    }

    @Command(names = {"isOnline"}, usage = "Usage: /<command> <player>", permission = "base.command.isonline", min = 1, max = 1)
    public boolean isOnlineCommand(CommandSender sender, CommandContext context) {
        ListenableFutureUtils.addCallback(userFinder.isOnline(context.getArgument(0), UserFinder.Scope.GLOBAL), online -> {
            String onlineStatus = MessageFormat.format(i18n.translate("user.is.online"), context.getArgument(0), i18n.translate("unknown"));

            if (online != null) {
                onlineStatus = MessageFormat.format(i18n.translate("user.is.online"), context.getArgument(0), online ? i18n.translate("online") : i18n.translate("offline"));
            }

            sender.sendMessage(onlineStatus);
        });

        return true;
    }


    @Command(names = {"currentserver", "cs"})
    public boolean currentServerCommand(CommandSender sender, CommandContext context) {
        sender.sendMessage(MessageFormat.format(i18n.translate("server.current"), serverData.getDisplayName()));
        return true;
    }

    @Command(names = {"listplayers", "serverplayers"}, usage = "Usage: /<command> [serverId]", max = 1, permission = "base.command.listplayers")
    public boolean listPlayersCommand(CommandSender sender, CommandContext context) {
        if (context.getArguments().size() == 0 && sender.hasPermission("base.command.listplayers.global")) {
            sender.sendMessage(i18n.translate("server.all.list"));

            ListenableFutureUtils.addCallback(serverManager.find(Integer.MAX_VALUE), servers -> {
                if (servers == null || servers.isEmpty()) {
                    String serverPlayers = i18n.translate("none");

                    if (!serverData.getOnlinePlayerNicks().isEmpty()) {
                        serverPlayers = String.join(", ", serverData.getOnlinePlayerNicks());
                    }

                    sender.sendMessage(MessageFormat.format(i18n.translate("server.players.list"), serverData.getId(), serverPlayers));
                    return;
                }

                servers.forEach(anotherServerData -> {
                    String serverPlayers = i18n.translate("none");
                    if (anotherServerData.isOnline() && !anotherServerData.getOnlinePlayerNicks().isEmpty()) {
                        serverPlayers = String.join(", ", anotherServerData.getOnlinePlayerNicks());
                    } else if (!anotherServerData.isOnline()) {
                        serverPlayers = i18n.translate("offline");
                    }

                    sender.sendMessage(MessageFormat.format(i18n.translate("server.players.list"), anotherServerData.getId(), serverPlayers));
                });
            });

            return true;
        } else if (!sender.hasPermission("base.command.listplayers.global")) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return false;
        }


        ListenableFutureUtils.addCallback(serverManager.findOne(context.getArgument(0)), server -> {
            if (server == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("server.not.found"), context.getArgument(0)));
                return;
            }

            String serverPlayers = i18n.translate("none");

            if (server.isOnline() && !server.getOnlinePlayerNicks().isEmpty()) {
                serverPlayers = String.join(", ", new ArrayList<>(server.getOnlinePlayerNicks()));
            } else if (!server.isOnline()) {
                serverPlayers = i18n.translate("offline");
            }

            sender.sendMessage(MessageFormat.format(i18n.translate("server.players.list"), server.getId(), serverPlayers));
        });

        return true;
    }
}
