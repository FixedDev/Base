package us.sparknetwork.base.handlers.user.settings;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.mongodb.client.MongoDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.BasePlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.datamanager.CachedMongoStorageProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static us.sparknetwork.utils.ListenableFutureUtils.addCallback;
import static us.sparknetwork.utils.ListenableFutureUtils.addOptionalToReturnValue;

@Singleton
public class UserSettingsHandlerImpl extends CachedMongoStorageProvider<UserSettings> implements Listener, UserSettingsHandler {

    @Inject
    private I18n i18n;
    @Inject
    private JavaPlugin plugin;

    private Map<String, UUID> by_nickname;

    @Inject
    UserSettingsHandlerImpl(ListeningExecutorService executorService, MongoDatabase database, RedissonClient redisson) {
        super(executorService, database, redisson, "user.settings", UserSettings.class);

        by_nickname = new ConcurrentHashMap<>();
    }


    @Override
    @Nullable
    public Player getPlayerByNick(String nickname){
        return Bukkit.getPlayer(by_nickname.get(nickname));
    }

    // Events //
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().sendMessage(i18n.translate("load.try.settings"));

        addCallback(addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), user -> {
            try {
                UserSettings userData = user.orElse(new BaseUserSettings(e.getPlayer()));

                if (userData.getNickname() != null) {
                    if (by_nickname.containsKey(userData.getNickname()) || Bukkit.getPlayer(userData.getNickname()) != null) {
                        userData.setNickname(null);
                    } else {
                        e.getPlayer().setDisplayName(userData.getNickname());
                        e.getPlayer().setPlayerListName(userData.getNickname());

                        by_nickname.put(userData.getNickname(), userData.getUniqueId());
                    }
                }

                save(userData);

                e.getPlayer().sendMessage(i18n.translate("load.sucess.settings"));
            } catch (Throwable ex) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    e.getPlayer().kickPlayer(i18n.translate("load.fail.settings"));
                });

                BasePlugin.logError(plugin.getLogger(), "load", "settings", e.getPlayer().getUniqueId().toString(), ex);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e) {
        addCallback(addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), user -> {
            UserSettings userData = user.orElse(new BaseUserSettings(e.getPlayer()));

            if(userData.getNickname() != null){
                by_nickname.remove(userData.getNickname());
            }

            this.save(userData);
        });
    }

}