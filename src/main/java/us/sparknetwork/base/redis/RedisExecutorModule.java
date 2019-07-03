package us.sparknetwork.base.redis;

import me.fixeddev.inject.ProtectedModule;

public class RedisExecutorModule extends ProtectedModule {

    @Override
    protected void configure() {
        bind(RedisExecutor.class).to(RedisExecutorImpl.class);

        expose(RedisExecutor.class);
    }
}
