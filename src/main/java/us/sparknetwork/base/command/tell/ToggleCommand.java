package us.sparknetwork.base.command.tell;

import com.google.inject.Inject;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.handlers.user.settings.UserSettings;
import us.sparknetwork.base.handlers.user.settings.UserSettingsHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;

import java.text.MessageFormat;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;
import static us.sparknetwork.utils.ListenableFutureUtils.addOptionalToReturnValue;

public class ToggleCommand implements CommandClass {

    @Inject
    private I18n i18n;

    @Inject
    private UserSettingsHandler settingsHandler;

    @Command(names = {"togglemessages", "togglemsg", "togglepm", "tpm", "toggleprivatemessages"}, max = 0, onlyPlayer = true, permission = "base.command.togglemessages", usage = "Usage: /<command>")
    public boolean toggleMessages(Player sender, CommandContext args) {
        addCallback(addOptionalToReturnValue(settingsHandler.findOne(sender.getUniqueId().toString())), optionalSettings -> {
            if (!optionalSettings.isPresent()) {
                return;
            }
            UserSettings settings = optionalSettings.get();

            settings.setPrivateMessagesVisible(!settings.isPrivateMessagesVisible());

            settingsHandler.save(settings);

            String bool = LangConfigurations.parseVisibility(i18n, settings.isPrivateMessagesVisible());

            sender.sendMessage(i18n.format("toggle.messages", sender.getName(), bool));
        });
        return true;
    }

}
