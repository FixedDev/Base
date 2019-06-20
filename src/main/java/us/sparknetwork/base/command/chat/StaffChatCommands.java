package us.sparknetwork.base.command.chat;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.messages.StaffChatMessage;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;


public class StaffChatCommands implements CommandClass {

    @Inject
    private I18n i18n;

    @Inject
    private UserHandler settingsHandler;

    private Channel<StaffChatMessage> messageChannel;

    @Inject
    StaffChatCommands(Messenger messenger) {
        messageChannel = messenger.getChannel("staffChat", StaffChatMessage.class);
    }

    @Command(names = {"staffchat", "sc"}, usage = "Usage: /<command> [text]", permission = "base.command.staffchat")
    public boolean staffChatCommand(CommandSender commandSender, CommandContext context) {
        if (!context.getArguments().isEmpty()) {
            String stringMessage = context.getJoinedArgs(0);

            if (commandSender.hasPermission("base.staffchat.color")) {
                stringMessage = ChatColor.translateAlternateColorCodes('&', stringMessage);
            }

            String staffFormattedMessage = MessageFormat.format(i18n.translate("chat.staff.format"), commandSender.getName(), stringMessage);

            Bukkit.getConsoleSender().sendMessage(staffFormattedMessage);

            Set<String> userIds = Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("base.command.staffchat.see")).map(Player::getUniqueId).map(UUID::toString).collect(Collectors.toSet());

            addCallback(settingsHandler.find(userIds, userIds.size()), userSettingsSet -> {
                userSettingsSet.stream().filter(Objects::nonNull).filter(User.ChatSettings::isStaffChatVisible).map(playerSettings -> Bukkit.getPlayer(playerSettings.getUUID())).forEach(player -> player.sendMessage(staffFormattedMessage));
            });

            StaffChatMessage message = new StaffChatMessage(commandSender.getName(), stringMessage);

            messageChannel.sendMessage(message);

            if (commandSender instanceof Player) {
                Player sender = (Player) commandSender;

                ListenableFutureUtils.addCallback(settingsHandler.findOne(sender.getUniqueId().toString()), settings -> {
                    if(settings == null){
                        return;
                    }

                    if(settings.isStaffChatVisible()){
                        return;
                    }

                    sender.sendMessage(i18n.translate("staff.chat.invisible"));
                });
            }

            return true;
        }

        if (!(commandSender instanceof Player)) {
            return false;
        }

        Player sender = (Player) commandSender;

        Optional<User.Complete> optionalSettings = Optional.ofNullable(settingsHandler.findOneSync(sender.getUniqueId().toString()));

        if (!optionalSettings.isPresent()) {
            return true;
        }

        User.Complete userSettings = optionalSettings.get();
        userSettings.setInStaffChat(!userSettings.isInStaffChat());

        settingsHandler.save(userSettings);

        String bool = userSettings.isInStaffChat() ? "StaffChat" : "Global";

        sender.sendMessage(i18n.format("toggle.staff.chat", sender.getName(), bool));

        return true;
    }

    @Command(names = {"togglestaffchat", "tsc", "togglesc"}, usage = "Usage: /<command>", max = 0, onlyPlayer = true, permission = "base.command.togglestaffchat")
    public boolean toggleStaffChat(Player sender, CommandContext context) {
        Optional<User.Complete> optionalSettings = Optional.ofNullable(settingsHandler.findOneSync(sender.getUniqueId().toString()));

        if (!optionalSettings.isPresent()) {
            return true;
        }

        User.Complete userSettings = optionalSettings.get();
        userSettings.setStaffChatVisible(!userSettings.isStaffChatVisible());

        settingsHandler.save(userSettings);

        String bool = LangConfigurations.parseVisibility(i18n, userSettings.isStaffChatVisible());

        sender.sendMessage(MessageFormat.format(i18n.translate("toggle.staff.chat.visibility"), sender.getName(), bool));

        return true;
    }
}
