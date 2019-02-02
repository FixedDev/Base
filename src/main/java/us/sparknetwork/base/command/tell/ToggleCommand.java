package us.sparknetwork.base.command.tell;

import com.google.inject.Inject;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;
import static us.sparknetwork.utils.ListenableFutureUtils.addOptionalToReturnValue;

public class ToggleCommand implements CommandClass {

    @Inject
    private I18n i18n;

    @Inject
    private UserHandler settingsHandler;

    @Command(names = {"messagesvisibility", "msgvisibility", "pmvisibility", "privatemessagesvisibility"}, max = 0, onlyPlayer = true, permission = "base.command.togglemessages", usage = "Usage: /<command>")
    public boolean toggleMessages(Player sender, CommandContext args) {
        addCallback(addOptionalToReturnValue(settingsHandler.findOne(sender.getUniqueId().toString())), optionalSettings -> {
            if (!optionalSettings.isPresent()) {
                return;
            }
            User.Complete settings = optionalSettings.get();

            User.WhisperVisibility nextVisiblity = getNextVisibility(settings.getPrivateMessagesVisibility());

            settings.setPrivateMessagesVisibility(nextVisiblity);
            settingsHandler.save(settings);

            String bool = LangConfigurations.parseWhisperVisibility(i18n, nextVisiblity);

            sender.sendMessage(i18n.format("toggle.messages", sender.getName(), bool));
        });
        return true;
    }


    private User.WhisperVisibility getNextVisibility(User.WhisperVisibility visibility) {
        switch (visibility) {
            case FRIENDS:
                return User.WhisperVisibility.NONE;
            case ALL:
                return User.WhisperVisibility.FRIENDS;
            case NONE:
            default:
                return User.WhisperVisibility.ALL;
        }
    }
}
