package us.sparknetwork.base.user.session;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginLogger;
import us.sparknetwork.base.datamanager.RedisStorageProvider;
import us.sparknetwork.base.redis.RedisExecutor;
import us.sparknetwork.utils.ListenableFutureUtils;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class SessionHandlerImpl extends RedisStorageProvider<Session, Session> implements SessionHandler {
    @Inject
    SessionHandlerImpl(RedisExecutor redisExecutor) {
        super(redisExecutor, "user:session");
    }

    @Inject
    private PluginLogger logger;

    @Override
    public ListenableFuture<Session> getSession(UUID id) {
        return findOne(id.toString());
    }

    @Override
    public ListenableFuture<Session> createSession(UUID id) {
        return ListenableFutureUtils.transformFuture(getSession(id), input -> {
            if (input == null) {
                input = new SessionImpl(id, ZonedDateTime.now(), Bukkit.getServerName());
                save(input);
            }

            return input;
        });
    }

    @Override
    public ListenableFuture<Void> deleteSession(UUID id) {
        return ListenableFutureUtils.transformFuture(getSession(id), input -> {
            if (input != null) {
                delete(input);
            }

            return null;
        });
    }

    @Override
    public ListenableFuture<Boolean> isOnline(UUID id) {
        return ListenableFutureUtils.transformFuture(getSession(id), Objects::nonNull);
    }
}
