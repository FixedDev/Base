package us.sparknetwork.base.id;

import com.google.inject.AbstractModule;

public class IdGeneratorModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(IdGenerator.class).to(RedisIdGenerator.class);
    }
}
