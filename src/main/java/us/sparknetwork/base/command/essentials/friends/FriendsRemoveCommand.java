package us.sparknetwork.base.command.essentials.friends;

import me.ggamer55.bcm.AbstractAdvancedCommand;
import me.ggamer55.bcm.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.util.ArrayList;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;

public class FriendsRemoveCommand extends AbstractAdvancedCommand {

    private UserHandler userHandler;
    private I18n i18n;

    FriendsRemoveCommand(UserHandler userHandler, I18n i18n) {
        super(new String[]{"remove", "delete"},
                "/<command> <player>",
                "",
                "",
                "No Permission.",
                new ArrayList<>(),
                1,
                1,
                false,
                new ArrayList<>());

        this.userHandler = userHandler;
        this.i18n = i18n;
    }


    @Override
    public boolean execute(CommandContext context) {
        CommandSender commandSender = context.getNamespace().getObject(CommandSender.class, "sender");

        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player sender = (Player) commandSender;

        OfflinePlayer target = Bukkit.getOfflinePlayer(context.getArgument(0));

        if (target.getUniqueId() == null) {
            commandSender.sendMessage(this.i18n.format("user.not.found", context.getArgument(0)));
            return true;
        }

        if (sender == target) {
            sender.sendMessage(i18n.translate("friends.cant.remove.self"));

            return true;
        }

        addCallback(ListenableFutureUtils.combine(this.userHandler.findOne(sender.getUniqueId().toString()), this.userHandler.findOne(target.getUniqueId().toString())), (data) -> {
            User.Complete from = data.getFirst();
            User.Complete to = data.getSecond();

            if (to == null) {
                commandSender.sendMessage(this.i18n.format("user.not.found", context.getArgument(0)));
                return;
            }

            if (!from.isFriendOf(to)) {
                commandSender.sendMessage(i18n.format("friends.not.already", to.hasNick() ? to.getNick() : to.getLastName()));
                return;
            }

            from.removeFriend(to);
            to.removeFriend(from);

            sender.sendMessage(i18n.format("friends.remove", to.hasNick() ? to.getNick() : to.getLastName(), from.getFriendsNumber()));
        });
        return true;
    }
}
