package us.sparknetwork.base.command.chat;

import com.google.inject.Inject;
import org.bukkit.entity.Player;
import us.sparknetwork.base.handlers.user.settings.UserSettingsHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;
import us.sparknetwork.utils.ListenableFutureUtils;

public class NickCommand implements CommandClass {

    @Inject
    private UserSettingsHandler settingsHandler;

    @Command(names = "nick", min = 1, permission = "base.command.nick", onlyPlayer = true)
    public boolean nickCommand(Player sender, CommandContext context) {
        ListenableFutureUtils.addCallback(settingsHandler.findOne(sender.getUniqueId().toString()), settings -> {
            String nickname = context.getArgument(0);

            if(nickname.length() >= 16){
                nickname = nickname.substring(0,16);
            }

            settings.setNickname(nickname);
            settingsHandler.save(settings);
            sender.setDisplayName(nickname);
        });

        return true;
    }
}
