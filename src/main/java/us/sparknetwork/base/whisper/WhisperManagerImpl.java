package us.sparknetwork.base.whisper;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.user.finder.UserFinder;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.messager.messages.WhisperMessage;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.util.*;
import java.util.stream.Collectors;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

@Singleton
public class WhisperManagerImpl implements WhisperManager {
    private I18n i18n;

    private UserFinder userFinder;
    private UserHandler userDataHandler;

    private ListeningExecutorService executorService;
    private Channel<WhisperMessage> whisperChannel;

    @Inject
    WhisperManagerImpl(I18n i18n, UserFinder userFinder, UserHandler userDataHandler, ListeningExecutorService executorService, Messenger messager) {
        this.i18n = i18n;
        this.userDataHandler = userDataHandler;
        this.userFinder = userFinder;
        this.executorService = executorService;
        whisperChannel = messager.getChannel("whisper", WhisperMessage.class);

        whisperChannel.registerListener((channel, serverSenderId, message) -> {
            sendSocialSpyMessage(message);
            sendMessage(message);
        });
    }

    public ListenableFuture<Void> sendMessageAsync(@NotNull Player sender, @NotNull User.Complete from, @NotNull User.Complete to, String content) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(to.getUUID());

        WhisperMessage message = new WhisperMessage(from, to, content);

        String senderNick = from.hasNick() ? from.getNick() : from.getLastName();
        String targetNick = to.hasNick() ? to.getNick() : to.getLastName();

        return ListenableFutureUtils.transformFutureAsync(userFinder.isOnline(to.getUUID(), UserFinder.Scope.GLOBAL), (online) -> {
            if (online == null) {
                online = false;
            }

            if (!online && !offlineTarget.isOnline()) { // I added the !offlineTarget.isOnline because in some cases the userFinder says that a user is offline even when it's online in the same server
                sender.sendMessage(i18n.format("offline.player", content));
                return Futures.immediateFuture(null);
            }

            if ((to.getPrivateMessagesVisibility() == User.WhisperVisibility.FRIENDS && !to.isFriendOf(from))
                    || to.getPrivateMessagesVisibility() == User.WhisperVisibility.NONE) {

                sender.sendMessage(i18n.format("pm.not.visible", targetNick));
                return Futures.immediateFuture(null);
            }

            if (to.isPlayerIgnored(sender.getUniqueId())) {
                sender.sendMessage(i18n.format("tell.format.to", senderNick, targetNick, content));
                return Futures.immediateFuture(null);
            }

            sendSocialSpyMessage(message);

            if (offlineTarget.isOnline()) {
                sendMessage(message);
            }

            whisperChannel.sendMessage(message);

            sender.sendMessage(i18n.format("tell.format.to", senderNick, targetNick, message.getMessage()));

            return Futures.immediateFuture(null);

        }, executorService);
    }

    private void sendMessage(WhisperMessage message) {
        OfflinePlayer offlineReceiver = Bukkit.getOfflinePlayer(message.getTo().getUUID());

        String senderNick = message.getFrom().hasNick() ? message.getFrom().getNick() : message.getFrom().getLastName();
        String receiverNick = message.getTo().hasNick() ? message.getTo().getNick() : message.getTo().getLastName();

        if (!offlineReceiver.isOnline()) {
            return;
        }

        Player receiver = offlineReceiver.getPlayer();
        receiver.sendMessage(i18n.format("tell.format.from", receiverNick, senderNick, message.getMessage()));

        addCallback(userDataHandler.findOne(message.getTo().getUUID().toString()), (userData) -> {
            userData.setLastPrivateMessageReplier(message.getFrom().getUUID());
            userDataHandler.save(userData);
        });
    }

    private void sendSocialSpyMessage(WhisperMessage message) {
        String senderNick = message.getFrom().hasNick() ? message.getFrom().getNick() : message.getFrom().getLastName();
        String receiverNick = message.getTo().hasNick() ? message.getTo().getNick() : message.getTo().getLastName();

        Set<String> userIds = Bukkit.getOnlinePlayers().stream()
                .filter(player -> player.hasPermission("base.socialspy.see"))
                .map(player -> player.getUniqueId().toString())
                .collect(Collectors.toSet());

        Bukkit.getConsoleSender().sendMessage(i18n.format("socialspy.format", senderNick, receiverNick, message.getMessage()));

        userDataHandler.findSync(userIds, userIds.size()).stream()
                .filter(User.WhisperSettings::isSocialSpyVisible)
                .map(complete -> Bukkit.getPlayer(complete.getUUID()))
                .forEach(player -> player.sendMessage(i18n.format("socialspy.format", senderNick, receiverNick, message.getMessage())));
    }
}