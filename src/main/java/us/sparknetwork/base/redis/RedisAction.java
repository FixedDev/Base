package us.sparknetwork.base.redis;

import org.redisson.Redisson;

public interface RedisAction {
    void executeAction(Redisson redisson);
}
