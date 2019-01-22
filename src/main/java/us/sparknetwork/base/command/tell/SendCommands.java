package us.sparknetwork.base.command.tell;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.exception.UserIgnoringYouException;
import us.sparknetwork.base.exception.UserMessagesDisabledException;
import us.sparknetwork.base.exception.UserOfflineException;
import us.sparknetwork.base.handlers.user.data.UserData;
import us.sparknetwork.base.handlers.user.data.UserDataHandler;
import us.sparknetwork.base.exception.UserNotFoundException;
import us.sparknetwork.base.handlers.user.finder.UserFinder;
import us.sparknetwork.base.handlers.user.settings.UserSettings;
import us.sparknetwork.base.handlers.user.settings.UserSettingsHandler;
import us.sparknetwork.base.handlers.whisper.WhisperManager;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.annotation.Command;
import us.sparknetwork.cm.command.arguments.CommandContext;
import us.sparknetwork.utils.BiSupplier;
import us.sparknetwork.utils.Callback;

import java.text.MessageFormat;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

public class SendCommands implements CommandClass {

    @Inject
    private I18n i18n;

    @Inject
    private UserSettingsHandler settingsHandler;

    @Inject
    private UserDataHandler dataHandler;

    @Inject
    private UserFinder userFinder;

    @Inject
    private WhisperManager whisperManager;

    @Command(names = {"tell", "msg", "whisper", "w", "m", "pm", "privatemessage", "message"}, min = 2, onlyPlayer = true, permission = "base.command.tell", usage = "Usage: /<command> <target> <message...>")
    public boolean tellCommand(Player sender, CommandContext context) {

        OfflinePlayer target = context.getObject(0, OfflinePlayer.class);

        if (target.getUniqueId() == null) {
            sender.sendMessage(MessageFormat.format(i18n.translate("user.not.found"), context.getArgument(0)));
            return true;
        }

        addCallback(combine(settingsHandler.findOne(sender.getUniqueId().toString()), settingsHandler.findOne(target.getUniqueId().toString())), data -> {
            UserSettings from = data.getFirst();
            UserSettings to = data.getSecond();

            if (to == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("user.not.found"), context.getArgument(0)));
                return;
            }

            addCallback(whisperManager.sendMessageAsync(sender, from, target.getUniqueId(), to, context.getJoinedArgs(1)), new Callback<Void>() {
                @Override
                public void call(Void object) {
                }

                @Override
                public void handleException(Throwable throwable) {
                    if (throwable instanceof UserOfflineException) {
                        sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), context.getArgument(0)));
                    } else if (throwable instanceof UserMessagesDisabledException) {
                        sender.sendMessage(MessageFormat.format(i18n.translate("pm.not.visible"), context.getArgument(0)));
                    } else if (throwable instanceof UserIgnoringYouException) {
                        sender.sendMessage(MessageFormat.format(i18n.translate("tell.format.to"), sender.getDisplayName(), target.getName(), context.getJoinedArgs(1)));
                    }
                }
            });

        });

        return true;
    }

    @Command(names = {"reply", "r", "tellreply", "whisperreply"}, min = 1, onlyPlayer = true, permission = "base.command.tell", usage = "Usage: /<command> <message...>")
    public boolean replyCommand(Player sender, CommandContext context) {
        addCallback(combine(dataHandler.findOne(sender.getUniqueId().toString()), settingsHandler.findOne(sender.getUniqueId().toString())), user -> {
            UserData senderData = user.getFirst();
            UserSettings senderSettings = user.getSecond();

            if (senderData.getLastPrivateMessageReplier() == null) {
                sender.sendMessage(i18n.translate("reply.target.unavailable"));
                return;
            }

            UserData targetData = dataHandler.findOneSync(senderData.getLastPrivateMessageReplier().toString());
            UserSettings targetSettings = settingsHandler.findOneSync(senderData.getLastPrivateMessageReplier().toString());


            if (targetData == null) {
                sender.sendMessage(MessageFormat.format(i18n.translate("user.not.found"), context.getArgument(0)));
                return;
            }

            addCallback(whisperManager.sendMessageAsync(sender, senderSettings, targetSettings.getUniqueId(), targetSettings, context.getJoinedArgs(0)), new Callback<Void>() {
                @Override
                public void call(Void object) {
                }

                @Override
                public void handleException(Throwable throwable) {
                    if (throwable instanceof UserOfflineException) {
                        sender.sendMessage(MessageFormat.format(i18n.translate("offline.player"), context.getArgument(0)));
                    } else if (throwable instanceof UserMessagesDisabledException) {
                        sender.sendMessage(MessageFormat.format(i18n.translate("pm.not.visible"), context.getArgument(0)));
                    } else if (throwable instanceof UserIgnoringYouException) {
                        String nick = targetSettings.getNickname();

                        if (nick == null) {
                            nick = targetData.getLastName();
                        }

                        sender.sendMessage(MessageFormat.format(i18n.translate("tell.format.to"), sender.getDisplayName(), nick, context.getJoinedArgs(0)));
                    }
                }
            });
        });
        return true;
    }

}
