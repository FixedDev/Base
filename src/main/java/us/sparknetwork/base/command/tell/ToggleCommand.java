package us.sparknetwork.base.command.tell;

import com.google.inject.Inject;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.handlers.user.User;
import us.sparknetwork.base.handlers.user.UserHandler;
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

    @Command(names = {"togglemessages", "togglemsg", "togglepm", "tpm", "toggleprivatemessages"}, max = 0, onlyPlayer = true, permission = "base.command.togglemessages", usage = "Usage: /<command>")
    public boolean toggleMessages(Player sender, CommandContext args) {
        addCallback(addOptionalToReturnValue(settingsHandler.findOne(sender.getUniqueId().toString())), optionalSettings -> {
            if (!optionalSettings.isPresent()) {
                return;
            }
            User.Complete settings = optionalSettings.get();

            settings.setPrivateMessagesVisible(!settings.getPrivateMessagesVisible());

            settingsHandler.save(settings);

            String bool = LangConfigurations.parseVisibility(i18n, settings.getPrivateMessagesVisible());

            sender.sendMessage(i18n.format("toggle.messages", sender.getName(), bool));
        });
        return true;
    }

}
