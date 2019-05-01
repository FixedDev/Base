package us.sparknetwork.base.inject;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.*;
import com.google.inject.name.Names;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.AllArgsConstructor;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.chat.BaseChatFormatManager;
import us.sparknetwork.base.chat.ChatFormatManager;
import us.sparknetwork.base.inject.annotations.PluginClassLoader;
import us.sparknetwork.base.inject.annotations.PluginDataFolder;
import us.sparknetwork.base.jackson.BukkitJacksonModule;
import us.sparknetwork.base.jackson.ScoreboardEntryModule;
import us.sparknetwork.base.server.LocalServerData;

import javax.annotation.Nullable;
import java.io.File;

@AllArgsConstructor
public class BasePluginModule extends AbstractModule {
    private JavaPlugin plugin;

    private LocalServerData serverData;

    private RedissonClient redisson;

    private MongoClient mongoClient;
    private MongoDatabase database;

    private ListeningExecutorService executorService;

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);

        bind(ClassLoader.class)
                .annotatedWith(PluginClassLoader.class)
                .toInstance(plugin.getClass().getClassLoader());

        bind(File.class)
                .annotatedWith(PluginDataFolder.class)
                .toInstance(plugin.getDataFolder());

        bind(ObjectMapper.class)
                .annotatedWith(Names.named("YMLMapper"))
                .toProvider(this::createMapper).in(Scopes.SINGLETON);

        bind(ObjectMapper.class)
                .toProvider(this::createMapper).in(Scopes.SINGLETON);

        bind(I18n.class);

        bind(ListeningExecutorService.class).toInstance(executorService);

        bind(Chat.class).toProvider(this::setupChat);

        bind(ChatFormatManager.class).to(BaseChatFormatManager.class);

        install(new ServerModule(serverData));

        install(new DatabaseModule(redisson, mongoClient, database));
        install(new HandlersModule());
        install(new CommandHandlerModule());
    }

    @Nullable
    private Chat setupChat() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return null;
        }
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return null;
        }
        return rsp.getProvider();
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC));

        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(new BukkitJacksonModule());
        mapper.registerModule(new ScoreboardEntryModule());

        return mapper;
    }

}
