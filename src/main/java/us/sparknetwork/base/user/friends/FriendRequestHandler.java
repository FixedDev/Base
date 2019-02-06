package us.sparknetwork.base.user.friends;

import com.google.common.util.concurrent.ListenableFuture;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.user.Identity;
import us.sparknetwork.base.user.User;

import java.util.Set;

public interface FriendRequestHandler {
    ListenableFuture<Set<FriendRequest>> getFriendRequestsFor(@NotNull Identity identity);

    ListenableFuture<FriendRequestReply> acceptFriendRequest(@NotNull Player accepter, @NotNull User.Complete from, @NotNull User.Complete to);

    ListenableFuture<FriendRequestReply> denyFriendRequest(@NotNull Player accepter, @NotNull User.Complete from, @NotNull User.Complete to);

    ListenableFuture<FriendRequest> sendFriendRequest(Player sender, @NotNull User.Complete from, @NotNull User.Complete to);

    ListenableFuture<Void> deleteFriendRequest(@NotNull String requestId);
}
