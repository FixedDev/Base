package us.sparknetwork.base.command.essentials.friends;

import me.fixeddev.bcm.AbstractAdvancedCommand;
import me.fixeddev.bcm.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.user.UserHandler;

import java.util.ArrayList;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;

public class FriendsSetLimitCommand extends AbstractAdvancedCommand {

    private UserHandler userHandler;

    private I18n i18n;

    FriendsSetLimitCommand(UserHandler userHandler, I18n i18n) {
        super(new String[]{"setlimit"},
                "/<command> <player> <limit>",
                "",
                "base.command.friends.setlimit",
                "No Permission.",
                new ArrayList<>(),
                2,
                2,
                false,
                new ArrayList<>());

        this.userHandler = userHandler;
        this.i18n = i18n;
    }

    @Override
    public boolean execute(CommandContext context) {
        CommandSender commandSender = context.getNamespace().getObject(CommandSender.class, "sender");

        OfflinePlayer target = Bukkit.getOfflinePlayer(context.getArgument(0));

        if (target.getUniqueId() == null) {
            commandSender.sendMessage(this.i18n.format("user.not.found", context.getArgument(0)));
            return true;
        }

        int limit;

        try {
            limit = Integer.parseInt(context.getArgument(1));
        } catch (NumberFormatException ex) {
            return false;
        }

        addCallback(userHandler.findOne(target.getUniqueId().toString()), object -> {
            if(object == null){
                commandSender.sendMessage(this.i18n.format("user.not.found", context.getArgument(0)));
                return;
            }

            object.setFriendsLimit(limit);
            userHandler.save(object);

            commandSender.sendMessage(i18n.format("friends.limit.set", object.hasNick() ? object.getNick() : object.getLastName(), limit));
        });

        return true;
    }
}
