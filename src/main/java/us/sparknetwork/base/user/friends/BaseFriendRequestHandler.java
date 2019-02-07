package us.sparknetwork.base.user.friends;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.datamanager.CachedMongoStorageProvider;
import us.sparknetwork.base.messager.Channel;
import us.sparknetwork.base.messager.Messenger;
import us.sparknetwork.base.user.Identity;
import us.sparknetwork.base.user.User;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.user.friends.listener.FriendRequestListener;
import us.sparknetwork.base.user.friends.listener.FriendsRequestReplyListener;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.util.Objects;
import java.util.Set;

import static com.mongodb.client.model.Filters.*;

@Singleton
public class BaseFriendRequestHandler extends CachedMongoStorageProvider<FriendRequest> implements FriendRequestHandler {

    @Inject
    private UserHandler userHandler;

    @Inject
    private I18n i18n;

    private Channel<FriendRequest> friendRequestChannel;
    private Channel<FriendRequestReply> friendRequestReplyChannel;

    private ListeningExecutorService executorService;

    @Inject
    BaseFriendRequestHandler(ListeningExecutorService executorService, MongoDatabase database, RedissonClient redisson, Messenger messenger, FriendRequestListener requestListener, FriendsRequestReplyListener replyListener) {
        super(executorService, database, redisson, "friend.requests", FriendRequest.class);
        this.executorService = executorService;

        friendRequestChannel = messenger.getChannel("friendRequest", FriendRequest.class);
        friendRequestReplyChannel = messenger.getChannel("friendRequestReply", FriendRequestReply.class);

        friendRequestChannel.registerListener(requestListener);
        friendRequestReplyChannel.registerListener(replyListener);
    }

    @Override
    public ListenableFuture<Set<FriendRequest>> getFriendRequestsFor(@NotNull Identity identity) {
        return this.findByQuery(eq("to", identity.getUUID()), 0, Integer.MAX_VALUE);
    }

    @Override
    public ListenableFuture<FriendRequestReply> acceptFriendRequest(@NotNull Player accepter, @NotNull User.Complete from, @NotNull User.Complete to) {
        Objects.requireNonNull(accepter);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        Bson query = and(eq("from", to.getUUID().toString()), eq("to", from.getUUID().toString()));

        return ListenableFutureUtils.transformFutureAsync(this.findOneByQuery(query), friendRequest -> {
            if (friendRequest == null) {
                accepter.sendMessage(i18n.format("friends.request.not.found", to.hasNick() ? to.getNick() : to.getLastName()));
                return Futures.immediateFuture(new FriendRequestReply(null, FriendRequestReply.RequestReply.NOT_FOUND));
            }

            OfflinePlayer requestSender = Bukkit.getOfflinePlayer(to.getUUID());

            from.addFriend(to);
            to.addFriend(from);

            delete(friendRequest);

            if (requestSender.isOnline()) {
                requestSender.getPlayer().sendMessage(i18n.format("friends.request.accepted", from.hasNick() ? from.getNick() : from.getLastName()));
            }

            FriendRequestReply requestReply = new FriendRequestReply(friendRequest, FriendRequestReply.RequestReply.ACCEPTED);
            friendRequestReplyChannel.sendMessage(requestReply);

            accepter.sendMessage(i18n.format("friends.request.accept", to.hasNick() ? to.getNick() : to.getLastName()));
            return Futures.immediateFuture(requestReply);
        }, executorService);
    }

    @Override
    public ListenableFuture<FriendRequestReply> denyFriendRequest(@NotNull Player denier, User.@NotNull Complete from, User.@NotNull Complete to) {
        Objects.requireNonNull(denier);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        Bson query = and(eq("from", to.getUUID().toString()), eq("to", from.getUUID().toString()));

        return ListenableFutureUtils.transformFutureAsync(this.findOneByQuery(query), friendRequest -> {
            if (friendRequest == null) {
                denier.sendMessage(i18n.format("friends.request.not.found", to.hasNick() ? to.getNick() : to.getLastName()));
                return Futures.immediateFuture(new FriendRequestReply(null, FriendRequestReply.RequestReply.NOT_FOUND));
            }

            OfflinePlayer requestSender = Bukkit.getOfflinePlayer(to.getUUID());

            if (requestSender.isOnline()) {
                requestSender.getPlayer().sendMessage(i18n.format("friends.request.denied", from.hasNick() ? from.getNick() : from.getLastName()));
            }

            FriendRequestReply requestReply = new FriendRequestReply(friendRequest, FriendRequestReply.RequestReply.DENIED);
            friendRequestReplyChannel.sendMessage(requestReply);

            delete(friendRequest);

            denier.sendMessage(i18n.format("friends.request.deny", to.hasNick() ? to.getNick() : to.getLastName()));
            return Futures.immediateFuture(requestReply);
        }, executorService);
    }

    @Override
    public ListenableFuture<FriendRequest> sendFriendRequest(Player sender, @NotNull User.Complete from, @NotNull User.Complete to) {
        Objects.requireNonNull(sender);
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        Bson query = and(eq("from", from.getUUID()), eq("to", to.getUUID()));

        return ListenableFutureUtils.transformFutureAsync(this.findOneByQuery(query), oldFriendRequest -> {
            if (oldFriendRequest != null) {
                sender.sendMessage(i18n.format("friends.request.already.send", to.hasNick() ? to.getNick() : to.getLastName()));
                return Futures.immediateFuture(null);
            }

            if (from.getFriendsNumber() >= from.getFriendsLimit()) {
                sender.sendMessage(i18n.format("friends.request.limit.reached.self", from.hasNick() ? from.getNick() : from.getLastName()));
                return Futures.immediateFuture(null);
            }

            if (to.getFriendsNumber() >= to.getFriendsLimit()) {
                sender.sendMessage(i18n.format("friends.request.limit.reached", to.hasNick() ? to.getNick() : to.getLastName()));
                return Futures.immediateFuture(null);
            }

            FriendRequest friendRequest = new FriendRequest(from.getUUID(), to.getUUID());

            friendRequestChannel.sendMessage(friendRequest);
            save(friendRequest);

            OfflinePlayer requestReceiver = Bukkit.getOfflinePlayer(to.getUUID());

            if (requestReceiver.isOnline()) {
                requestReceiver.getPlayer().sendMessage(i18n.format("friends.request.receive", from.hasNick() ? from.getNick() : from.getLastName()));
            }

            sender.sendMessage(i18n.format("friends.request.send", to.hasNick() ? to.getNick() : to.getLastName()));

            return Futures.immediateFuture(friendRequest);
        }, executorService);
    }

    @Override
    public ListenableFuture<Void> deleteFriendRequest(@NotNull String requestId) {
        return delete(requestId);
    }
}
