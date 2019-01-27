package us.sparknetwork.base.command.chat;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.handlers.user.UserHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

public class NickCommand implements CommandClass {
    @Inject
    private UserHandler dataHandler;
    @Inject
    private JavaPlugin plugin;
    @Inject
    private I18n i18n;

    @Command(names = {"nick"}, min = 1, max = 2, permission = "base.command.nick", usage = "Usage: /<command> [player] <nick | off>")
    public boolean nickCommand(CommandSender sender, CommandContext context) {
        if (context.getArguments().size() == 1 && !(sender instanceof Player)) {
            return false;
        }

        Player target;
        String originalNick;

        if (context.getArguments().size() == 1) {
            target = (Player) sender;
            originalNick = context.getArgument(0);
        } else if (context.getArguments().size() == 2) {
            target = context.getObject(0, Player.class);
            originalNick = context.getArgument(1);

            if (target == null) {
                sender.sendMessage(this.i18n.format("offline.player", context.getArgument(0)));
                return true;
            }
        } else {
            return false;
        }

        addCallback(this.dataHandler.findOne(target.getUniqueId().toString()), (settings) -> {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (originalNick.equalsIgnoreCase("off")) {
                    settings.setNick(null);

                    target.setDisplayName(target.getName());
                    target.setPlayerListName(target.getName());

                    this.dataHandler.save(settings);

                    sender.sendMessage(this.i18n.format("nick.disabled", settings.getLastName()));
                } else {
                    String nick = originalNick;

                    if (originalNick.length() >= 48) {
                        nick = originalNick.substring(0, 48);
                    }

                    if (this.dataHandler.getPlayerByNick(nick) != null || Bukkit.getPlayer(nick) != null) {
                        sender.sendMessage(this.i18n.format("nick.already.used", originalNick));

                        return;
                    }

                    String oldNick = settings.hasNick() ? settings.getNick() : this.i18n.translate("none");

                    settings.setNick(nick);
                    target.setDisplayName(nick);
                    target.setPlayerListName(nick);

                    this.dataHandler.save(settings);

                    sender.sendMessage(this.i18n.format("nick.changed", settings.getLastName(), oldNick, nick));
                }
            });
        });
        return true;

    }
}
