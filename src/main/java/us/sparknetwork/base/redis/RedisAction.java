package us.sparknetwork.base.redis;

import org.redisson.Redisson;

public interface RedisAction<V> {
    V executeAction(Redisson redisson);
}
