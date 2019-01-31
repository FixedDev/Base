

package us.sparknetwork.base;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.base.server.ServerRole;
import us.sparknetwork.base.server.ServerVisibility;
import us.sparknetwork.utils.Config;

public class ServerConfigurations {

    // instance fields
    private JavaPlugin plugin;
    @Getter
    private static ServerConfigurations instance;
    private Config serverConfig;

    // configuration fields
    public static boolean HOOK_PROTOCOL_LIB = false;
    public static String LANGUAGE = "en";

    public static String DEFAULT_MESSAGER_CHANNEL = "network";

    public static long MUTED_CHAT = 0;

    public static long SLOW_CHAT = 0;
    public static int SLOW_CHAT_DELAY = 3;

    public static ServerRole SERVER_ROLE = ServerRole.OTHER;
    public static ServerVisibility SERVER_VISIBILIY = ServerVisibility.PUBLIC;

    public ServerConfigurations(JavaPlugin plugin) {
        if (instance != null) {
            throw new RuntimeException("ServerConfiguration instance already exists");
        }
        this.plugin = plugin;
        serverConfig = new Config(plugin, "config");

        instance = this;

        HOOK_PROTOCOL_LIB = serverConfig.getBoolean("hook-protocollib", HOOK_PROTOCOL_LIB);
        LANGUAGE = serverConfig.getString("language", LANGUAGE);
        MUTED_CHAT = serverConfig.getLong("chat.muted", MUTED_CHAT);
        SLOW_CHAT = serverConfig.getLong("chat.slowed", SLOW_CHAT);
        SLOW_CHAT_DELAY = serverConfig.getInt("chat.slow-delay", SLOW_CHAT_DELAY);

        SERVER_ROLE = ServerRole.valueOf(serverConfig.getString("server.role", SERVER_ROLE.toString()));
        SERVER_VISIBILIY = ServerVisibility.valueOf(serverConfig.getString("server.visibility", SERVER_VISIBILIY.toString()));


        this.saveConfig();
    }

    public void saveConfig() {
        this.serverConfig.set("server.role", SERVER_ROLE.toString());
        this.serverConfig.set("server.visibility", SERVER_VISIBILIY.toString());
        this.serverConfig.set("hook-protocollib", HOOK_PROTOCOL_LIB);
        this.serverConfig.set("language", LANGUAGE);
        this.serverConfig.set("chat.muted", MUTED_CHAT);
        this.serverConfig.set("chat.slowed", SLOW_CHAT);
        this.serverConfig.set("chat.slow-delay", SLOW_CHAT_DELAY);
        this.serverConfig.save();
    }
}
