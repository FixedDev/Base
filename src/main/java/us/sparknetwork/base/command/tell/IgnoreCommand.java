

package us.sparknetwork.base.command.tell;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.handlers.user.User;
import us.sparknetwork.base.handlers.user.UserHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;
import static us.sparknetwork.utils.ListenableFutureUtils.addOptionalToReturnValue;


public class IgnoreCommand implements CommandClass {

    @Inject
    private I18n i18n;

    @Inject
    private UserHandler settingsHandler;

    @Command(names = {"ignore", "ignoreplayer", "playerignore"}, max = 1, onlyPlayer = true, permission = "base.command.ignore", usage = "Usage: /<command> <player>")
    public boolean ignorePlayer(Player sender, CommandContext args) {
        addCallback(addOptionalToReturnValue(settingsHandler.findOne(sender.getUniqueId().toString())), optionalSettings -> {
            if (!optionalSettings.isPresent()) {
                return;
            }

            User.Complete settings = optionalSettings.get();

            if (args.getArguments().size() == 0) {
                List<UUID> ignoredPlayers = settings.getIgnoredPlayers();

                if (ignoredPlayers.isEmpty()) {
                    sender.sendMessage(i18n.translate("ignoring.nobody"));

                    return;
                }

                sender.sendMessage(i18n.translate("ignoring.to"));

                ignoredPlayers.stream()
                        .map(Bukkit::getOfflinePlayer)
                        .forEach(ignored -> {
                            String playerNick = ignored.isOnline() ? ignored.getPlayer().getDisplayName() : ignored.getName();
                            sender.sendMessage(MessageFormat.format(i18n.translate("ignoring.list.player"), playerNick));
                        });


                return;
            }

            Player target = args.getObject(0, Player.class);

            if (target == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), args.getArgument(0)));
                return;
            }

            if (target.hasPermission("base.command.ignore.bypass")) {
                sender.sendMessage(MessageFormat.format(i18n.translate("cant.ignore.player"), target.getDisplayName()));
                return;
            }

            if (settings.isPlayerIgnored(target.getUniqueId())) {
                settings.removeIgnoredPlayer(target.getUniqueId());

                sender.sendMessage(MessageFormat.format(i18n.translate("not.ignored.player"), target.getDisplayName()));
                return;
            }

            settings.addIgnoredPlayer(target.getUniqueId());
            sender.sendMessage(MessageFormat.format(i18n.translate("ignored.player"), target.getDisplayName()));

            return;
        });

        return true;
    }
}
