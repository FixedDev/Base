package us.sparknetwork.base.id;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLongAdder;
import org.redisson.api.RedissonClient;

@Singleton
public class RedisIdGenerator implements IdGenerator {

    @Inject
    private RedissonClient client;
    @Inject
    private ListeningExecutorService executorService;

    @Override
    public long getNextId(String type) {
        RAtomicLong atomicLong = client.getAtomicLong(type);

        return atomicLong.addAndGet(1);
    }

    @Override
    public ListenableFuture<Long> getNextIdAsync(String type) {
        return executorService.submit(() -> getNextId(type));
    }

}
