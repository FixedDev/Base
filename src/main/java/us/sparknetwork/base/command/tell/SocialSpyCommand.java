package us.sparknetwork.base.command.tell;

import com.google.inject.Inject;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.handlers.user.UserHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.text.MessageFormat;
import java.util.UUID;

public class SocialSpyCommand implements CommandClass {

    @Inject
    private I18n i18n;
    @Inject
    private UserHandler settingsHandler;

    @Command(names = "socialspy", max = 1, permission = "base.command.socialspy", usage = "Usage: /<command> [player]")
    public boolean socialSpy(CommandSender sender, CommandContext context) {
        String targetNick;
        UUID target;
        if (!(sender instanceof Player)) {
            if (context.getArguments().size() == 0) {
                return false;
            }

            Player playerTarget = context.getObject(0, Player.class);

            if (playerTarget == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), context.getArgument(0)));
                return true;
            }

            target = playerTarget.getUniqueId();
            targetNick = playerTarget.getDisplayName();
        } else if (context.getArguments().size() == 1) {
            Player playerTarget = context.getObject(0, Player.class);

            if (playerTarget == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), context.getArgument(0)));
                return true;
            }

            target = playerTarget.getUniqueId();
            targetNick =  playerTarget.getDisplayName();
        } else {
            Player commandSender = (Player) sender;

            target = commandSender.getUniqueId();
            targetNick = commandSender.getDisplayName();
        }

        ListenableFutureUtils.addCallback(settingsHandler.findOne(target.toString()), settings -> {
            settings.setSocialSpyVisible(!settings.isSocialSpyVisible());
            settingsHandler.save(settings);

            String bool = LangConfigurations.convertBoolean(i18n, settings.isSocialSpyVisible());

            sender.sendMessage(MessageFormat.format(i18n.translate("socialspy"), targetNick, bool));
        });

        return true;
    }
}
