package us.sparknetwork.base.command.chat;

import com.google.inject.Inject;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;
import static us.sparknetwork.utils.ListenableFutureUtils.addOptionalToReturnValue;

public class ToggleChatCommand implements CommandClass {

    @Inject
    private I18n i18n;

    @Inject
    private UserHandler settingsHandler;

    @Command(names = {"toggleglobalchat", "tgc", "togglechat"}, usage = "Usage: /<command>", max = 0, onlyPlayer = true, permission = "base.command.toggleglobalchat")
    public boolean toggleGlobalChat(Player sender, CommandContext context) {
        addCallback(addOptionalToReturnValue(settingsHandler.findOne(sender.getUniqueId().toString())), optionalSettings -> {
            if (!optionalSettings.isPresent()) {
                return;
            }
            User.Complete userSettings = optionalSettings.get();

            userSettings.setGlobalChatVisible(!userSettings.isGlobalChatVisible());

            settingsHandler.save(userSettings);

            String bool = LangConfigurations.parseVisibility(i18n, userSettings.isGlobalChatVisible());

            sender.sendMessage(MessageFormat.format(i18n.translate("toggle.global.chat"), sender.getName(), bool));
        });

        return true;
    }


}
