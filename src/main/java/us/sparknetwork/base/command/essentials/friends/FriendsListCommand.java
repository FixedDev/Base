package us.sparknetwork.base.command.essentials.friends;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import me.ggamer55.bcm.AbstractAdvancedCommand;
import me.ggamer55.bcm.CommandContext;
import me.ggamer55.bcm.basic.exceptions.CommandException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.user.finder.UserFinder;
import us.sparknetwork.utils.JsonMessage;
import us.sparknetwork.utils.ListenableFutureUtils;
import us.sparknetwork.utils.TemporaryCommandUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;

public class FriendsListCommand extends AbstractAdvancedCommand {

    private UserHandler userHandler;
    private UserFinder userFinder;
    private I18n i18n;
    private TemporaryCommandUtils temporaryCommandUtils;

    FriendsListCommand(UserHandler userHandler, UserFinder userFinder, I18n i18n, TemporaryCommandUtils temporaryCommandUtils) {
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
        this.userFinder = userFinder;
        this.i18n = i18n;
        this.temporaryCommandUtils = temporaryCommandUtils;
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

            System.out.println(user.getFriendsNumber());
            System.out.println(i18n.translate("none"));

            if (user.getFriendsNumber() == 0) {
                message.save().append(i18n.translate("none"));
            }

            Iterator<User.Complete> userFriendsIterator = userFriends.iterator();

            while (userFriendsIterator.hasNext()) {
                User.Complete userFriend = userFriendsIterator.next();

                String friendNick;

                ListenableFuture<Boolean> isOnline = userFinder.isOnline(userFriend.getUUID(), UserFinder.Scope.GLOBAL);

                try {
                    Boolean online = isOnline.get(1, TimeUnit.SECONDS);

                    if (online) {
                        friendNick = i18n.translate("friends.list.online") + userFriend.getLastName();
                    } else {
                        friendNick = i18n.translate("friends.list.offline") + userFriend.getLastName();
                    }

                    message = message.save().append(friendNick);

                    if (online) {
                        String randomId = UUID.randomUUID().toString();

                        temporaryCommandUtils.registerTemporalCommand(sender, randomId, player -> {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "send " + player.getName() + " " + userFriend.getLastServerId());
                        });

                        message = message
                                .setHoverAsTooltip(i18n.format("friends.list.hover", userFriend.getLastServerId()).split("\n"))
                                .setClickAsExecuteCmd("/" + randomId)
                                .save().append("");
                    }

                    if (userFriendsIterator.hasNext()) {
                        message = message.save().append(i18n.translate("friends.list.delimiter"));
                    }

                } catch (InterruptedException | TimeoutException | ExecutionException ex) {
                    sender.sendMessage(i18n.translate("error.ocurred"));

                    if (!(ex instanceof TimeoutException)) {
                        Bukkit.getLogger().log(Level.SEVERE, "There was an error while retrieving online state of " + userFriend.getId());
                    }
                }
            }

            message.save().send(sender);
        });
        return true;
    }
}
