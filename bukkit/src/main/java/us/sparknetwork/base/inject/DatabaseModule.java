package us.sparknetwork.base.inject;

import com.google.inject.AbstractModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;

@AllArgsConstructor
public class DatabaseModule extends AbstractModule {
    private RedissonClient redissonClient;

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    @Override
    protected void configure() {
        bind(RedissonClient.class).toInstance(redissonClient);

        bind(MongoClient.class).toInstance(mongoClient);
        bind(MongoDatabase.class).toInstance(mongoDatabase);
    }
}
