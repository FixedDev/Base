package us.sparknetwork.base.id;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
        RLongAdder adder = client.getLongAdder(type);
        adder.increment();

        return adder.sum();
    }

    @Override
    public ListenableFuture<Long> getNextIdAsync(String type) {
        return executorService.submit(() -> {
            RLongAdder adder = client.getLongAdder(type);
            adder.increment();

            return adder.sum();
        });
    }

}
