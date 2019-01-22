package us.sparknetwork.base.handlers.user.finder;

import com.google.common.util.concurrent.ListenableFuture;
import us.sparknetwork.base.handlers.server.Server;

import java.util.UUID;

public interface UserFinder {

    enum Scope {
        LOCAL, GLOBAL
    }

    ListenableFuture<Boolean> isOnline(UUID playerUUID, Scope scope);

    ListenableFuture<Boolean> isOnline(String playerNick, Scope scope);

    ListenableFuture<Server> findUser(UUID playerUUID, Scope scope);

    ListenableFuture<Server> findUser(String playerNick, Scope scope);
}
