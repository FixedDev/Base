package us.sparknetwork.base;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import fr.javatic.mongo.jacksonCodec.JacksonCodecProvider;
import fr.javatic.mongo.jacksonCodec.ObjectMapperFactory;
import lombok.Getter;
import me.ggamer55.bcm.bukkit.BukkitCommandHandler;
import net.milkbowl.vault.chat.Chat;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.SingleServerConfig;
import us.sparknetwork.base.chat.ChatFormatManager;
import us.sparknetwork.base.command.chat.BroadcastCommand;
import us.sparknetwork.base.command.chat.ModerationCommands;
import us.sparknetwork.base.command.chat.NickCommand;
import us.sparknetwork.base.command.chat.StaffChatCommands;
import us.sparknetwork.base.command.chat.ToggleChatCommand;
import us.sparknetwork.base.command.essentials.HelpopCommands;
import us.sparknetwork.base.command.essentials.KickAllCommand;
import us.sparknetwork.base.command.essentials.PlayerCommands;
import us.sparknetwork.base.command.essentials.PunishmentCommands;
import us.sparknetwork.base.command.essentials.RestartCommands;
import us.sparknetwork.base.command.essentials.SendCommand;
import us.sparknetwork.base.command.essentials.ServerCommands;
import us.sparknetwork.base.command.essentials.StaffCommands;
import us.sparknetwork.base.command.essentials.TeleportCommands;
import us.sparknetwork.base.command.essentials.WorldCommands;
import us.sparknetwork.base.command.essentials.friends.FriendsMainCommand;
import us.sparknetwork.base.command.inventory.InventoryCommands;
import us.sparknetwork.base.command.inventory.InvseeCommand;
import us.sparknetwork.base.command.inventory.ItemCommands;
import us.sparknetwork.base.command.tell.IgnoreCommand;
import us.sparknetwork.base.command.tell.SendCommands;
import us.sparknetwork.base.command.tell.SocialSpyCommand;
import us.sparknetwork.base.command.tell.ToggleCommand;
import us.sparknetwork.base.datamanager.redisson.RedissonJsonJacksonCodec;
import us.sparknetwork.base.hooks.PlaceholderAPIHook;
import us.sparknetwork.base.hooks.ProtocolLibHook;
import us.sparknetwork.base.inject.BasePluginModule;
import us.sparknetwork.base.listeners.ChatListener;
import us.sparknetwork.base.listeners.JoinFullServer;
import us.sparknetwork.base.listeners.JoinMessageListener;
import us.sparknetwork.base.listeners.PunishmentListener;
import us.sparknetwork.base.module.ModuleHandler;
import us.sparknetwork.base.module.ModuleHandlerModule;
import us.sparknetwork.base.restart.RestartManager;
import us.sparknetwork.base.restart.RestartPriority;
import us.sparknetwork.base.server.LocalServerData;
import us.sparknetwork.base.server.MongoServerManager;
import us.sparknetwork.base.server.ServerRole;
import us.sparknetwork.base.server.type.LocalGameServer;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.CommandHandler;
import us.sparknetwork.utils.Config;
import us.sparknetwork.utils.TemporaryCommandUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BasePluginLoader {

    private BasePlugin plugin;

    // Non injected fields
    private RedissonClient redisson;

    private MongoClient mongoClient;
    private MongoDatabase database;

    private ListeningExecutorService executorService;

    // Injected fields
    @Inject
    private ModuleHandler moduleHandler;
    @Inject
    private CommandHandler commandHandler;

    @Inject
    private UserHandler userHandler;

    @Inject
    private MongoServerManager serverManager;

    @Inject
    private I18n i18n;

    private Chat chat = null;

    @Getter
    private Injector injector;

    BasePluginLoader(BasePlugin plugin, ListeningExecutorService executorService) {
        this.plugin = plugin;
        this.executorService = executorService;
    }

    public void onEnable() {
        if (!setupChat()) {
            plugin.getLogger().severe("Failed to load Vault Chat API, disabling plugin.");
            plugin.setPluginEnabled(false);
            return;
        }

        registerHandlers();

        LocalServerData serverData;

        if (ServerConfigurations.SERVER_ROLE == ServerRole.GAME) {
            serverData = new LocalGameServer(Bukkit.getServerName(), Bukkit.getIp(), Bukkit.getPort(), ServerConfigurations.SERVER_GAME_ID);
        } else {
            serverData = new LocalServerData(Bukkit.getServerName(), Bukkit.getIp(), Bukkit.getPort(), true);
        }


        injector = Guice.createInjector(Stage.PRODUCTION, new BasePluginModule(plugin, serverData, chat, redisson, mongoClient, database, executorService), new ModuleHandlerModule(ModuleHandler.getLoadedModules()));
        injector.injectMembers(this);

        try {
            startServices();
        } catch (Exception e) {
            BasePlugin.logError(plugin.getLogger(), "start", "services", null, e);
        }

        registerCommands();
        registerEvents();

        if (plugin.getServer().getPluginManager().getPlugin("ProtocolLib") != null && ServerConfigurations.HOOK_PROTOCOL_LIB) {
            new ProtocolLibHook().addListeners(plugin);
            plugin.getLogger().info("ProtocolLib hook enabled");
        }

        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook.hookPlaceholders(injector);
            plugin.getLogger().info("PlaceholderAPI hook enabled");
        }

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    public void onDisable() {
        stopServices();

        if (injector != null) {
            injector = null;
        }

        if (mongoClient != null) {
            mongoClient.close();
        }

        if (redisson != null) {
            redisson.shutdown();
        }

        executorService.shutdown();
    }

    private void startRedis(Config config) {
        String password = config.getString("redis.password", "");

        org.redisson.config.Config redissonConfig = new org.redisson.config.Config()
                .setExecutor(executorService);

        SingleServerConfig serverConfig = redissonConfig.useSingleServer()
                .setAddress("redis://" + config.getString("redis.host", "localhost") + ":" + config.getInt("redis.port", 6379));


        ObjectMapper objectMapper = new ObjectMapper();

        configurateObjectMapper(objectMapper);

        redissonConfig.setCodec(new RedissonJsonJacksonCodec(objectMapper, false));

        if (!password.trim().isEmpty()) {
            serverConfig.setPassword(password);
        }

        redisson = Redisson.create(redissonConfig);

    }

    private void startMongo(Config config) {
        String connectionString;
        if (config.getString("mongo.auth.user", "").isEmpty() || config.getString("mongo.auth.password", "").isEmpty()) {
            connectionString = "mongodb://{host}:{port}/";
        } else {
            connectionString = "mongodb://{user}:{password}@{host}:{port}/?authSource={database}";
        }

        connectionString = connectionString
                .replace("{user}", config.getString("mongo.auth.user", ""))
                .replace("{password}", config.getString("mongo.auth.password", ""))
                .replace("{host}", config.getString("mongo.host", "localhost"))
                .replace("{port}", config.getInt("mongo.port", 27017) + "")
                .replace("{database}", config.getString("mongo.database", "network"));

        ConnectionString connectionStringObject = new ConnectionString(connectionString);

        mongoClient = MongoClients.create(MongoClientSettings.builder()
                .applyConnectionString(connectionStringObject)
                .applyToSslSettings(builder -> builder.enabled(config.getBoolean("mongo.ssl", false)))
                .build());


        database = mongoClient.getDatabase(config.getString("mongo.database", "network"));

        ObjectMapper mapper = ObjectMapperFactory.createObjectMapper();

        configurateObjectMapper(mapper);

        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(database.getCodecRegistry(),
                CodecRegistries.fromProviders(new JacksonCodecProvider(mapper)));

        database = database.withCodecRegistry(codecRegistry);

    }

    private void configurateObjectMapper(ObjectMapper mapper) {
        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.NONE)
                .withGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withIsGetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withSetterVisibility(JsonAutoDetect.Visibility.ANY)
                .withCreatorVisibility(JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC));

        mapper.configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        TypeFactory tf = TypeFactory.defaultInstance().withClassLoader(plugin.getPluginClassLoader());
        mapper.setTypeFactory(tf);

        mapper.registerModule(new JavaTimeModule());
    }

    private void registerHandlers() {
        Config dbConfig = new Config(plugin, "database");

        startRedis(dbConfig);
        startMongo(dbConfig);

        new ServerConfigurations(plugin);
    }

    private void startServices() throws Exception {
        moduleHandler.start();
        serverManager.start();

        ChatFormatManager chatFormatManager = injector.getInstance(ChatFormatManager.class);
        chatFormatManager.start();

        RestartManager restartManager = injector.getInstance(RestartManager.class);
        restartManager.start();

        restartManager.scheduleRestartIn(Duration.ofMillis(ServerConfigurations.RESTART_TIME), RestartPriority.NORMAL);
    }

    private void stopServices() {
        if (injector == null) {
            return;
        }
        moduleHandler.stop();
        serverManager.stop();

        ChatFormatManager chatFormatManager = injector.getInstance(ChatFormatManager.class);
        chatFormatManager.stop();

        RestartManager restartManager = injector.getInstance(RestartManager.class);
        restartManager.stop();
    }

    private void registerCommands() {
        List<CommandClass> commandClasses = new ArrayList<>();

        commandClasses.add(injector.getInstance(BroadcastCommand.class));
        commandClasses.add(injector.getInstance(ModerationCommands.class));
        commandClasses.add(injector.getInstance(ToggleChatCommand.class));
        commandClasses.add(injector.getInstance(StaffChatCommands.class));
        commandClasses.add(injector.getInstance(NickCommand.class));

        commandClasses.add(injector.getInstance(KickAllCommand.class));
        commandClasses.add(injector.getInstance(StaffCommands.class));
        commandClasses.add(injector.getInstance(TeleportCommands.class));
        commandClasses.add(injector.getInstance(ServerCommands.class));
        commandClasses.add(injector.getInstance(WorldCommands.class));

        commandClasses.add(injector.getInstance(InvseeCommand.class));
        commandClasses.add(injector.getInstance(InventoryCommands.class));
        commandClasses.add(injector.getInstance(ItemCommands.class));

        commandClasses.add(injector.getInstance(IgnoreCommand.class));
        commandClasses.add(injector.getInstance(SocialSpyCommand.class));
        commandClasses.add(injector.getInstance(SendCommands.class));
        commandClasses.add(injector.getInstance(ToggleCommand.class));

        for (CommandClass commandClass : commandClasses) {
            commandHandler.registerCommandClass(commandClass);
        }

        BukkitCommandHandler newCommandHandler = new BukkitCommandHandler(plugin.getLogger());

        newCommandHandler.registerCommand(injector.getInstance(FriendsMainCommand.class));
        newCommandHandler.registerCommandClass(injector.getInstance(SendCommand.class));
        newCommandHandler.registerCommandClass(injector.getInstance(RestartCommands.class));
        newCommandHandler.registerCommandClass(injector.getInstance(HelpopCommands.class));
        newCommandHandler.registerCommandClass(injector.getInstance(PlayerCommands.class));
        newCommandHandler.registerCommandClass(injector.getInstance(PunishmentCommands.class));
    }


    private void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(userHandler, plugin);

        plugin.getServer().getPluginManager().registerEvents(serverManager, plugin);

        plugin.getServer().getPluginManager().registerEvents(injector.getInstance(ChatListener.class), plugin);
        plugin.getServer().getPluginManager().registerEvents(injector.getInstance(JoinMessageListener.class), plugin);
        plugin.getServer().getPluginManager().registerEvents(injector.getInstance(InvseeCommand.class), plugin);

        plugin.getServer().getPluginManager().registerEvents(injector.getInstance(TemporaryCommandUtils.class), plugin);

        plugin.getServer().getPluginManager().registerEvents(injector.getInstance(JoinFullServer.class), plugin);

        plugin.getServer().getPluginManager().registerEvents(injector.getInstance(PunishmentListener.class), plugin);
    }


    private boolean setupChat() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        chat = rsp.getProvider();
        return chat != null;
    }


}
