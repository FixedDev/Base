package us.sparknetwork.base.handlers.user.data;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.mongodb.client.MongoDatabase;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.BasePlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.datamanager.CachedMongoStorageProvider;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.logging.Level;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

@Singleton
public class UserDataHandler extends CachedMongoStorageProvider<UserData> implements Listener {

    @Inject
    private I18n i18n;
    @Inject
    private JavaPlugin plugin;

    @Inject
    UserDataHandler(ListeningExecutorService executorService, MongoDatabase database, RedissonClient redisson) {
        super(executorService, database, redisson, "user.data", UserData.class);
    }

    // Events //
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage(i18n.translate("load.try.data"));

        addCallback(addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), user -> {
            try {
                UserData userData = user.orElse(new BaseUserData(e.getPlayer()));

                userData.tryAddAdress(e.getPlayer().getAddress().getAddress().getHostAddress());
                userData.tryAddName(e.getPlayer().getName());

                userData.setLastServerId(Bukkit.getServerName());

                userData.setOnline(true);

                save(userData);

                e.getPlayer().sendMessage(i18n.translate("load.sucess.data"));
            } catch (Throwable ex) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    e.getPlayer().kickPlayer(i18n.translate("load.fail.data"));
                });

                BasePlugin.logError(plugin.getLogger(), "load", "data", e.getPlayer().getUniqueId().toString(), ex);
            }
        });

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e) {
        addCallback(addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), user -> {
            if (!user.isPresent()) {
                plugin.getLogger().log(Level.SEVERE, "Failed to find user data on quit event nick: {0} id: {1} ", new Object[]{
                        e.getPlayer().getName(),
                        e.getPlayer().getUniqueId()
                });
                return;
            }
            UserData userData = user.get();

            userData.setLastJoin(System.currentTimeMillis());
            userData.setOnline(false);

            this.save(userData);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        addCallback(addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), user -> {
            if (!user.isPresent()) {
                e.getPlayer().sendMessage(i18n.translate("load.fail.data"));
            }

            UserData data = user.orElse(new BaseUserData(e.getPlayer()));
            data.setLastSpeakTime(System.currentTimeMillis());

            this.save(data);
        });

    }
}
