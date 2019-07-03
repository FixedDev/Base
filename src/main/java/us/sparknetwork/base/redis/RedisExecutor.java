package us.sparknetwork.base.redis;

public interface RedisExecutor {
    void submitSync(RedisAction action);

    void submit(RedisAction action);
}
