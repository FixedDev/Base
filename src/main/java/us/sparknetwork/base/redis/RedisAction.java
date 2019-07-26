package us.sparknetwork.base.redis;

import org.redisson.api.RedissonClient;

public interface RedisAction<V> {
    V executeAction(RedissonClient redisson);
}
