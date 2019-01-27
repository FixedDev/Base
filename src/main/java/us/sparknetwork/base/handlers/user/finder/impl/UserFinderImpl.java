package us.sparknetwork.base.handlers.user.finder.impl;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import us.sparknetwork.base.handlers.server.Server;
import us.sparknetwork.base.handlers.server.LocalServerData;
import us.sparknetwork.base.handlers.server.ServerManager;
import us.sparknetwork.base.handlers.user.finder.UserFinder;

import java.util.Set;
import java.util.UUID;

import us.sparknetwork.utils.ListenableFutureUtils;

public class UserFinderImpl implements UserFinder {

    @Inject
    private ServerManager serverManager;
    @Inject
    private LocalServerData localServerData;

    @Override
    public ListenableFuture<Boolean> isOnline(UUID playerUUID, Scope scope) {
        AsyncFunction<Server, Boolean> transformFunction = server -> Futures.immediateFuture(server != null);

        return ListenableFutureUtils.transformFutureAsync(findUser(playerUUID, scope), transformFunction, MoreExecutors.newDirectExecutorService());
    }

    @Override
    public ListenableFuture<Boolean> isOnline(String playerNick, Scope scope) {
        AsyncFunction<Server, Boolean> transformFunction = server -> Futures.immediateFuture(server != null);

        return ListenableFutureUtils.transformFutureAsync(findUser(playerNick, scope), transformFunction, MoreExecutors.newDirectExecutorService());
    }

    @Override
    public ListenableFuture<Server> findUser(UUID playerUUID, Scope scope) {
        Preconditions.checkNotNull(scope);
        Preconditions.checkNotNull(playerUUID);

        if (scope == Scope.LOCAL && Bukkit.getOfflinePlayer(playerUUID).isOnline()) {
            return Futures.immediateFuture(localServerData);
        }

        AsyncFunction<Set<Server>, Server> transformFunction = servers -> Futures.immediateFuture(servers.stream().filter(server -> server.getOnlinePlayerIds().contains(playerUUID)).findFirst().orElse(null));

        return ListenableFutureUtils.transformFutureAsync(serverManager.find(Integer.MAX_VALUE), transformFunction, MoreExecutors.newDirectExecutorService());
    }

    @Override
    public ListenableFuture<Server> findUser(String playerNick, Scope scope) {
        Preconditions.checkNotNull(scope);
        Preconditions.checkArgument(StringUtils.isNotBlank(playerNick));

        if (scope == Scope.LOCAL && Bukkit.getOfflinePlayer(playerNick).isOnline()) {
            return Futures.immediateFuture(localServerData);
        }

        AsyncFunction<Set<Server>, Server> transformFunction = servers -> Futures.immediateFuture(servers.stream().filter(server -> server.getOnlinePlayerNicks().contains(playerNick)).findFirst().orElse(null));

        return ListenableFutureUtils.transformFutureAsync(serverManager.find(Integer.MAX_VALUE), transformFunction, MoreExecutors.newDirectExecutorService());
    }
}
