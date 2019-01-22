package us.sparknetwork.base.command.chat;

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
import java.util.Optional;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

public class StaffChatCommands implements CommandClass {

    @Inject
    private I18n i18n;

    @Inject
    private UserSettingsHandler settingsHandler;

    @Command(names = {"staffchat", "sc"}, usage = "Usage: /<command>", max = 0, onlyPlayer = true, permission = "base.command.staffchat")
    public boolean staffChatCommand(Player sender, CommandContext context) {
        Optional<UserSettings> optionalSettings = Optional.ofNullable(settingsHandler.findOneSync(sender.getUniqueId().toString()));

        if(!optionalSettings.isPresent()){
            return true;
        }

        UserSettings userSettings = optionalSettings.get();
        userSettings.setInStaffChat(!userSettings.isInStaffChat());

        settingsHandler.save(userSettings);

        String bool = userSettings.isInStaffChat() ? "StaffChat" : "Global";

        sender.sendMessage(MessageFormat.format(i18n.translate("toggle.staff.chat"), sender.getName(), bool));

        return true;
    }

    @Command(names = {"togglestaffchat", "tsc", "togglesc"}, usage = "Usage: /<command>", max = 0, onlyPlayer = true, permission = "base.command.togglestaffchat")
    public boolean toggleStaffChat(Player sender, CommandContext context) {
        Optional<UserSettings> optionalSettings = Optional.ofNullable(settingsHandler.findOneSync(sender.getUniqueId().toString()));

        if(!optionalSettings.isPresent()){
            return true;
        }
        UserSettings userSettings = optionalSettings.get();
        userSettings.setStaffChatVisible(!userSettings.isStaffChatVisible());

        settingsHandler.save(userSettings);

        String bool = LangConfigurations.parseVisibility(i18n, userSettings.isStaffChatVisible());

        sender.sendMessage(MessageFormat.format(i18n.translate("toggle.staff.chat.visibility"), sender.getName(), bool));

        return true;
    }
}
