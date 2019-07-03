package us.sparknetwork.base.redis;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.redisson.Redisson;

import java.util.concurrent.ExecutorService;

@Singleton
public class RedisExecutorImpl implements RedisExecutor {

    @Inject
    private Redisson redisson;
    @Inject
    private ExecutorService executorService;

    @Override
    public void submit(RedisAction action) {
        executorService.submit(() -> action.executeAction(redisson));
    }

    @Override
    public void submitSync(RedisAction action) {
        action.executeAction(redisson);
    }
}
