package us.sparknetwork.base.command.essentials.friends;

import me.fixeddev.bcm.AbstractAdvancedCommand;
import me.fixeddev.bcm.CommandContext;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.user.friends.FriendRequestHandler;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.util.ArrayList;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;

public class FriendsAddCommand extends AbstractAdvancedCommand {

    private UserHandler userHandler;
    private FriendRequestHandler requestHandler;
    private I18n i18n;

    FriendsAddCommand(UserHandler userHandler, FriendRequestHandler requestHandler, I18n i18n) {
        super(new String[]{"add"},
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
        this.requestHandler = requestHandler;
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
            sender.sendMessage(i18n.translate("friends.cant.add.self"));

            return true;
        }

        addCallback(ListenableFutureUtils.combine(this.userHandler.findOne(sender.getUniqueId().toString()), this.userHandler.findOne(target.getUniqueId().toString())), (data) -> {
            User.Complete from = data.getFirst();
            User.Complete to = data.getSecond();

            if (to == null) {
                commandSender.sendMessage(this.i18n.format("user.not.found", context.getArgument(0)));
                return;
            }

            if (to.isFriendOf(from)) {
                commandSender.sendMessage(i18n.format("friends.already", to.hasNick() ? to.getNick() : to.getLastName()));
                return;
            }

            this.requestHandler.sendFriendRequest(sender, from, to);
        });
        return true;
    }
}
