package us.sparknetwork.base.redis;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.redisson.api.RedissonClient;

@Singleton
public class RedisExecutorImpl implements RedisExecutor {

    @Inject
    private RedissonClient redisson;
    @Inject
    private ListeningExecutorService executorService;

    @Override
    public <V> ListenableFuture<V> submit(RedisAction<V> action) {
        return executorService.submit(() -> action.executeAction(redisson));
    }

    @Override
    public <V> V submitSync(RedisAction<V> action) {
        return action.executeAction(redisson);
    }
}
