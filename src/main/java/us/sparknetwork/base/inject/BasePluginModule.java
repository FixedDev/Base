package us.sparknetwork.base.inject;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.Gson;
import com.google.inject.*;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.chat.BaseChatFormatManager;
import us.sparknetwork.base.chat.ChatFormatManager;
import us.sparknetwork.base.handlers.server.LocalServerData;
import us.sparknetwork.utils.Config;

import java.lang.reflect.Field;

@AllArgsConstructor
public class BasePluginModule extends AbstractModule {
    private JavaPlugin plugin;

    private LocalServerData serverData;

    private Chat chat;

    private Gson gson;

    private RedissonClient redisson;

    private MongoClient mongoClient;
    private MongoDatabase database;

    private ListeningExecutorService executorService;

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);
        bind(Gson.class).toInstance(gson);
        bind(ListeningExecutorService.class).toInstance(executorService);

        bind(Chat.class).toInstance(chat);

        bind(ChatFormatManager.class).to(BaseChatFormatManager.class);

        install(new ServerModule(serverData));

        install(new DatabaseModule(redisson, mongoClient, database));
        install(new HandlersModule());
        install(new CommandHandlerModule());

    }
}
