package us.sparknetwork.base;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.AbstractModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.chat.ChatFormatModule;
import us.sparknetwork.base.inject.BasePluginModule;
import us.sparknetwork.base.redis.RedisExecutorModule;
import us.sparknetwork.base.restart.RestartManagerModule;
import us.sparknetwork.base.server.LocalServerData;
import us.sparknetwork.base.server.ServerManagerModule;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@AllArgsConstructor
public class BaseModule extends AbstractModule {

    private BasePlugin plugin;
    private LocalServerData serverData;
    private RedissonClient redisson;
    private MongoClient client;
    private MongoDatabase database;

    @Override
    protected void configure() {
        bind(ListeningExecutorService.class).toInstance(MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10)));
        bind(ExecutorService.class).to(ListeningExecutorService.class);

        install(new CommandManagerModule());

        install(new BasePluginModule(plugin, serverData, redisson, client, database));

        install(new ChatFormatModule());
        install(new RestartManagerModule());
        install(new ServerManagerModule());
        install(new RedisExecutorModule());
    }
}
