package us.sparknetwork.base.command.essentials.friends;

import com.google.inject.Inject;
import me.fixeddev.bcm.AbstractAdvancedCommand;
import me.fixeddev.bcm.CommandContext;
import me.fixeddev.bcm.basic.ArgumentArray;
import me.fixeddev.bcm.basic.Namespace;
import org.bukkit.command.CommandSender;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.user.finder.UserFinder;
import us.sparknetwork.base.user.friends.FriendRequestHandler;
import us.sparknetwork.utils.TemporaryCommandUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FriendsMainCommand extends AbstractAdvancedCommand {
    @Inject
    FriendsMainCommand(I18n i18n, UserHandler userHandler, UserFinder finder, FriendRequestHandler requestHandler, TemporaryCommandUtils temporaryCommandUtils) {
        super(new String[]{"friends"});

        setUsage("/<command> <subcommand>");

        this.registerSubCommand(new FriendsAcceptCommand(userHandler, requestHandler, i18n));
        this.registerSubCommand(new FriendsDenyCommand(userHandler, requestHandler, i18n));
        this.registerSubCommand(new FriendsAddCommand(userHandler, requestHandler, i18n));
        this.registerSubCommand(new FriendsRemoveCommand(userHandler, i18n));
        this.registerSubCommand(new FriendsListCommand(userHandler, finder, i18n, temporaryCommandUtils));
        this.registerSubCommand(new FriendsSetLimitCommand(userHandler, i18n));

    }

    @Override
    public boolean execute(CommandContext context) {
        return false;
    }

    @Override
    public List<String> getSuggestions(Namespace namespace, ArgumentArray arguments) {
        CommandSender sender = namespace.getObject(CommandSender.class, "sender");

        List<String> basicCommands = new ArrayList<>(Arrays.asList("accept", "deny", "add", "remove", "list"));

        if (sender.hasPermission("base.command.friends.setlimit")) {
            basicCommands.add("setlimit");
        }

        return basicCommands;
    }
}
