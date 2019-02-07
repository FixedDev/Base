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
import us.sparknetwork.utils.JsonMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;

public class FriendsListCommand extends AbstractAdvancedCommand {

    private UserHandler userHandler;
    private I18n i18n;

    FriendsListCommand(UserHandler userHandler, I18n i18n) {
        super(new String[]{"list"},
                "/<command>",
                "",
                "",
                "No Permission.",
                new ArrayList<>(),
                0,
                0,
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

        addCallback(this.userHandler.findOne(sender.getUniqueId().toString()), (user) -> {
            if (user == null) {
                return;
            }

            Set<String> friends = user.getFriends().stream().map(UUID::toString).collect(Collectors.toSet());

            Set<User.Complete> userFriends = userHandler.findSync(friends, friends.size());

            JsonMessage.JsonStringBuilder message = new JsonMessage().append(i18n.translate("friends.list.prefix"));
            message = message.save().append("");

            if(user.getFriendsNumber() == 0){
                message.save().append(i18n.translate("none"));
            }

             Iterator<User.Complete> userFriendsIterator = userFriends.iterator();

            while (userFriendsIterator.hasNext()){
                User.Complete userFriend = userFriendsIterator.next();

                String friendNick;

                if (userFriend.isOnline()) {
                    friendNick = i18n.translate("friends.list.online") + userFriend.getLastName();
                } else {
                    friendNick = i18n.translate("friends.list.offline") + userFriend.getLastName();
                }

                message = message.save().append(friendNick);

                if (userFriend.isOnline()) {
                    message = message.setHoverAsTooltip(i18n.format("friends.list.hover", userFriend.getLastServerId()).split("\n")).save().append("");
                }

                if(userFriendsIterator.hasNext()){
                    message = message.save().append(i18n.translate("friends.list.delimiter"));
                }
            }

            message.save().send(sender);
        });
        return true;
    }
}
