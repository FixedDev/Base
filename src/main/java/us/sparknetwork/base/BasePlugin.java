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
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import fr.javatic.mongo.jacksonCodec.JacksonCodecProvider;
import fr.javatic.mongo.jacksonCodec.ObjectMapperFactory;
import lombok.Getter;

import me.fixeddev.inject.ServiceManager;
import me.fixeddev.bcm.parametric.ParametricCommandRegistry;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.SingleServerConfig;
import us.sparknetwork.base.command.chat.*;
import us.sparknetwork.base.command.essentials.*;
import us.sparknetwork.base.command.essentials.friends.FriendsMainCommand;
import us.sparknetwork.base.command.inventory.InventoryCommands;
import us.sparknetwork.base.command.inventory.InvseeCommand;
import us.sparknetwork.base.command.inventory.ItemCommands;
import us.sparknetwork.base.command.tell.IgnoreCommand;
import us.sparknetwork.base.command.tell.SendCommands;
import us.sparknetwork.base.command.tell.SocialSpyCommand;
import us.sparknetwork.base.command.tell.ToggleCommand;
import us.sparknetwork.base.datamanager.redisson.RedissonJsonJacksonCodec;
import us.sparknetwork.base.listeners.JoinFullListener;
import us.sparknetwork.base.listeners.PunishmentListener;
import us.sparknetwork.base.server.LocalServerData;
import us.sparknetwork.base.server.ServerRole;
import us.sparknetwork.base.server.type.LocalGameServer;
import us.sparknetwork.base.user.UserHandler;
import us.sparknetwork.base.hooks.PlaceholderAPIHook;
import us.sparknetwork.base.hooks.ProtocolLibHook;
import us.sparknetwork.base.listeners.ChatListener;
import us.sparknetwork.base.listeners.JoinMessageListener;
import us.sparknetwork.cm.CommandClass;
import us.sparknetwork.cm.CommandHandler;
import us.sparknetwork.utils.Config;
import us.sparknetwork.utils.TemporaryCommandUtils;

import java.util.*;
import java.util.logging.Level;

public class BasePlugin extends JavaPlugin {

    public static final UUID CONSOLE_UUID = UUID.fromString("fecac143-508a-4c25-845c-dab5b9b4a03f");

    private RedissonClient redisson;

    private MongoClient mongoClient;
    private MongoDatabase database;

    private ListeningExecutorService executorService;

    // Injected fields
    @Inject
    private CommandHandler commandHandler;

    @Inject
    private ParametricCommandRegistry commandRegistry;

    @Inject
    private UserHandler userHandler;

    @Inject
    private I18n i18n;

    @Getter
    private Injector injector;

    @Inject
    private ServiceManager serviceManager;

    @Override
    public void onLoad() {
        if (!this.getDataFolder().exists() && !this.getDataFolder().mkdir()) {
            getLogger().log(Level.WARNING, "The data folder was not created even if it doesn't exist, maybe we don't have permissions?");
        }
    }

    @Override
    public void onEnable() {
        this.registerHandlers();

        LocalServerData serverData = new LocalServerData(Bukkit.getServerName(), Bukkit.getIp(), Bukkit.getPort(), true);

        if (ServerConfigurations.SERVER_ROLE == ServerRole.GAME) {
            serverData = new LocalGameServer(Bukkit.getServerName(), Bukkit.getIp(), Bukkit.getPort(), ServerConfigurations.SERVER_GAME_ID);
        }

        injector = Guice.createInjector(new BaseModule(this, serverData, redisson, mongoClient, database));
        injector.injectMembers(this);

        serviceManager.start();

        this.registerCommands();
        this.registerEvents();

        if (this.getServer().getPluginManager().getPlugin("ProtocolLib") != null && ServerConfigurations.HOOK_PROTOCOL_LIB) {
            new ProtocolLibHook().addListeners(this);
            this.getLogger().info("ProtocolLib hook enabled");
        }

        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            PlaceholderAPIHook.hookPlaceholders(injector);
            this.getLogger().info("PlaceholderAPI hook enabled");
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

    }

    @Override
    public void onDisable() {
        serviceManager.stop();

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
        if (config.getString("mongo.auth.user", "").isEmpty() || getConfig().getString("mongo.auth.password", "").isEmpty()) {
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
                .applyToSslSettings(builder -> builder.enabled(getConfig().getBoolean("mongo.ssl", false)))
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

        TypeFactory tf = TypeFactory.defaultInstance().withClassLoader(this.getClassLoader());
        mapper.setTypeFactory(tf);

        mapper.registerModule(new JavaTimeModule());
    }

    private void registerHandlers() {
        Config dbConfig = new Config(this, "database");

        startRedis(dbConfig);
        startMongo(dbConfig);

        new ServerConfigurations(this);
    }

    private void registerCommands() {
        List<CommandClass> commandClasses = new ArrayList<>();

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

        commandRegistry.registerCommand(injector.getInstance(FriendsMainCommand.class));
        commandRegistry.registerCommandClass(injector.getInstance(SendCommand.class));
        commandRegistry.registerCommandClass(injector.getInstance(RestartCommands.class));
        commandRegistry.registerCommandClass(injector.getInstance(HelpopCommands.class));
        commandRegistry.registerCommandClass(injector.getInstance(PlayerCommands.class));
        commandRegistry.registerCommandClass(injector.getInstance(PunishmentCommands.class));
        commandRegistry.registerCommandClass(injector.getInstance(BroadcastCommand.class));
    }


    private void registerEvents() {
        this.getServer().getPluginManager().registerEvents(userHandler, this);

        this.getServer().getPluginManager().registerEvents(injector.getInstance(ChatListener.class), this);
        this.getServer().getPluginManager().registerEvents(injector.getInstance(JoinMessageListener.class), this);
        this.getServer().getPluginManager().registerEvents(injector.getInstance(InvseeCommand.class), this);

        this.getServer().getPluginManager().registerEvents(injector.getInstance(TemporaryCommandUtils.class), this);

        this.getServer().getPluginManager().registerEvents(injector.getInstance(JoinFullListener.class), this);

        this.getServer().getPluginManager().registerEvents(injector.getInstance(PunishmentListener.class), this);
    }


}
