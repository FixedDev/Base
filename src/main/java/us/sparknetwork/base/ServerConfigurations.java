

package us.sparknetwork.base;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import us.sparknetwork.utils.Config;

import java.util.UUID;

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

    public static String SERVER_DISPLAY_NAME = Bukkit.getServerName();

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

        SERVER_DISPLAY_NAME= serverConfig.getString("server-display-name", SERVER_DISPLAY_NAME);

        this.saveConfig();
    }

    public void saveConfig() {
        this.serverConfig.set("hook-protocollib", HOOK_PROTOCOL_LIB);
        this.serverConfig.set("language", LANGUAGE);
        this.serverConfig.set("chat.muted", MUTED_CHAT);
        this.serverConfig.set("chat.slowed", SLOW_CHAT);
        this.serverConfig.set("chat.slow-delay", SLOW_CHAT_DELAY);
        this.serverConfig.set("server-display-name", SERVER_DISPLAY_NAME);
        this.serverConfig.save();
    }
}
