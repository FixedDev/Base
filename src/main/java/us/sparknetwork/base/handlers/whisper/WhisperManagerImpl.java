package us.sparknetwork.base.handlers.whisper;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.exception.UserIgnoringYouException;
import us.sparknetwork.base.exception.UserMessagesDisabledException;
import us.sparknetwork.base.exception.UserNotFoundException;
import us.sparknetwork.base.exception.UserOfflineException;
import us.sparknetwork.base.handlers.server.Server;
import us.sparknetwork.base.handlers.user.data.UserDataHandler;
import us.sparknetwork.base.handlers.user.finder.UserFinder;
import us.sparknetwork.base.handlers.user.settings.UserSettings;
import us.sparknetwork.base.handlers.user.settings.UserSettingsHandler;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.messages.WhisperMessage;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

@Singleton
public class WhisperManagerImpl implements WhisperManager {

    private I18n i18n;

    private PluginManager pluginManager;

    private JavaPlugin plugin;

    private UserFinder userFinder;

    private UserDataHandler userDataHandler;

    private UserSettingsHandler userSettingsHandler;

    private Server serverData;

    private ListeningExecutorService executorService;

    private Channel<WhisperMessage> whisperChannel;

    private Queue<WhisperMessage> messageQueue;

    @Inject
    WhisperManagerImpl(I18n i18n, PluginManager pluginManager, JavaPlugin plugin, UserFinder userFinder, UserDataHandler userDataHandler, UserSettingsHandler userSettingsHandler, Server serverData, ListeningExecutorService executorService, Messenger messager) {
        this.i18n = i18n;
        this.pluginManager = pluginManager;
        this.plugin = plugin;
        this.userFinder = userFinder;
        this.userDataHandler = userDataHandler;
        this.userSettingsHandler = userSettingsHandler;
        this.serverData = serverData;
        this.executorService = executorService;

        messageQueue = new LinkedList<>();

        whisperChannel = messager.getChannel("whisper", WhisperMessage.class);

        whisperChannel.registerListener((channel, serverSenderId, data) -> messageQueue.add(data));

        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            while (!messageQueue.isEmpty()) {
                WhisperMessage message = messageQueue.poll();

                OfflinePlayer offlineReceiver = Bukkit.getOfflinePlayer(message.getReceiver());

                String senderNick = message.getSenderNick();

                if (senderNick == null) {
                    senderNick = message.getSenderName();
                }

                String receiverNick = offlineReceiver.getName();

                if (offlineReceiver.isOnline()) {
                    receiverNick = offlineReceiver.getPlayer().getDisplayName();
                }

                Set<String> userIds = Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.hasPermission("base.socialspy.see"))
                        .map(player -> player.getUniqueId().toString())
                        .collect(Collectors.toSet());

                Bukkit.getConsoleSender().sendMessage(MessageFormat.format(i18n.translate("socialspy.format"), senderNick, receiverNick, message.getMessage()));

                Set<UserSettings> userSettingsSet = userSettingsHandler.findSync(userIds, userIds.size());

                for(UserSettings settings : userSettingsSet){
                    if(!settings.isSocialSpyVisible()){
                        continue;
                    }

                    Player player = Bukkit.getPlayer(settings.getUniqueId());

                    player.sendMessage(MessageFormat.format(i18n.translate("socialspy.format"), senderNick, receiverNick, message.getMessage()));
                }


                if (offlineReceiver.isOnline()) {
                    Player receiver = offlineReceiver.getPlayer();

                    String displayName = message.getSenderNick();

                    if (displayName == null) {
                        displayName = message.getSenderName();
                    }

                    receiver.sendMessage(MessageFormat.format(i18n.translate("tell.format.from"), receiver.getDisplayName(), displayName, message.getMessage()));
                    addCallback(userDataHandler.findOne(message.getReceiver().toString()), userData -> {
                        userData.setLastPrivateMessageReplier(message.getSender());

                        userDataHandler.save(userData);
                    });
                }

            }
        }, 10, 10);
    }

    @Override
    public ListenableFuture<Void> sendMessageAsync(@NonNull Player sender, @NonNull UserSettings from, UUID target, UserSettings to, String content) {
        if (to == null) {
            return Futures.immediateFailedFuture(new UserNotFoundException(target));
        }
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(to.getUniqueId());

        WhisperMessage message = new WhisperMessage(sender.getUniqueId(), sender.getName(), from.getNickname(), Bukkit.getServerName(), to.getUniqueId(), content);

        if (offlineTarget.isOnline()) {
            if (!to.isPrivateMessagesVisible()) {
                return Futures.immediateFailedFuture(new UserMessagesDisabledException(to.getUniqueId(), offlineTarget.getName()));
            }

            if (to.isPlayerIgnored(sender.getUniqueId())) {
                return Futures.immediateFailedFuture(new UserIgnoringYouException(to.getUniqueId(), offlineTarget.getName()));
            }

            messageQueue.add(message);

            String nick = to.getNickname();

            if (nick == null) {
                nick = offlineTarget.getName();
            }

            sender.sendMessage(MessageFormat.format(i18n.translate("tell.format.to"), sender.getDisplayName(), nick, message.getMessage()));

            return Futures.immediateFuture(null);
        }

        return transformFutureAsync(userFinder.isOnline(to.getUniqueId(), UserFinder.Scope.GLOBAL), online -> {
            if (!online) {
                return Futures.immediateFailedFuture(new UserOfflineException(to.getUniqueId(), offlineTarget.getName()));
            }

            if (!to.isPrivateMessagesVisible()) {
                return Futures.immediateFailedFuture(new UserMessagesDisabledException(to.getUniqueId(), offlineTarget.getName()));
            }

            if (to.isPlayerIgnored(sender.getUniqueId())) {
                return Futures.immediateFailedFuture(new UserIgnoringYouException(to.getUniqueId(), offlineTarget.getName()));
            }

            whisperChannel.sendMessage(message);

            String nick = to.getNickname();

            if (nick == null) {
                nick = offlineTarget.getName();
            }

            sender.sendMessage(MessageFormat.format(i18n.translate("tell.format.to"), sender.getDisplayName(), nick, message.getMessage()));

            return Futures.immediateFuture(null);
        }, executorService);
    }


}
