package us.sparknetwork.base.redis;

import com.google.common.util.concurrent.ListenableFuture;

public interface RedisExecutor {
    <V> V submitSync(RedisAction<V> action);

    <V> ListenableFuture<V> submit(RedisAction<V> action);
}
