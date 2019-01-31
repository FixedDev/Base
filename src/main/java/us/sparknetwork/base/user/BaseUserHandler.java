package us.sparknetwork.base.user;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.BasePlugin;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.StaffPriority;
import us.sparknetwork.base.datamanager.CachedMongoStorageProvider;
import us.sparknetwork.base.event.UserNickChangeEvent;
import us.sparknetwork.base.user.User.State;

import us.sparknetwork.utils.ListenableFutureUtils;

import static us.sparknetwork.utils.ListenableFutureUtils.*;

public class BaseUserHandler extends CachedMongoStorageProvider<User.Complete> implements UserHandler {
    @Inject
    private JavaPlugin plugin;
    @Inject
    private I18n i18n;

    private Map<String, UUID> by_nickname = new ConcurrentHashMap<>();

    @Inject
    BaseUserHandler(ListeningExecutorService executorService, MongoDatabase database, RedissonClient redisson) {
        super(executorService, database, redisson, "user", User.Complete.class);
    }

    static void updateVanishState(Player player, Collection<? extends Player> collection, boolean vanished) {
        player.spigot().setCollidesWithEntities(!vanished);

        for (Player viewer : collection) {
            if (!player.equals(viewer)) {
                if (vanished) {
                    if (StaffPriority.getByPlayer(player).isMoreThan(StaffPriority.getByPlayer(viewer))) {
                        viewer.hidePlayer(player);
                    }
                } else {
                    viewer.showPlayer(player);
                }
            }
        }
    }

    @Nullable
    public Player getPlayerByNick(@NotNull String nick) {
        UUID playerId = this.by_nickname.get(nick);

        return playerId == null ? null : Bukkit.getPlayer(playerId);
    }

    @EventHandler
    public void onNickChange(UserNickChangeEvent e) {
        if (e.getOldNick() != null) {
            this.by_nickname.remove(e.getOldNick());
        }

        if (e.getNewNick() != null) {
            this.by_nickname.put(e.getNewNick(), e.getUser().getUUID());
        }

    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e) {
        ListenableFutureUtils.addCallback(ListenableFutureUtils.addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), (user) -> {
            try {
                User.Complete userData = user.orElse(new BaseUser(e.getPlayer().getUniqueId()));
                userData.setLastServerId(Bukkit.getServerName());
                userData.setOnline(true);
                this.save(userData);
            } catch (Throwable var4) {
                e.disallow(Result.KICK_OTHER, this.i18n.translate("load.fail.data"));
                BasePlugin.logError(this.plugin.getLogger(), "load", "data", e.getPlayer().getUniqueId().toString(), var4);
            }

        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) {
        ListenableFutureUtils.addCallback(ListenableFutureUtils.addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), (user) -> {
            try {
                User.Complete userData = user.orElseThrow(Exception::new);

                userData.tryAddAdress(e.getPlayer().getAddress().getAddress().getHostAddress());
                userData.tryAddName(e.getPlayer().getName());

                if (userData.getNick() != null) {
                    if (!this.by_nickname.containsKey(userData.getNick()) && Bukkit.getPlayer(userData.getNick()) == null) {

                        e.getPlayer().setDisplayName(userData.getNick());
                        e.getPlayer().setPlayerListName(userData.getNick());

                        this.by_nickname.put(userData.getNick(), userData.getUUID());
                    } else {

                        userData.setNick(null);

                        e.getPlayer().setDisplayName(userData.getLastName());
                        e.getPlayer().setPlayerListName(userData.getLastName());
                    }
                }

                this.save(userData);
            } catch (Throwable var4) {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    e.getPlayer().kickPlayer(this.i18n.translate("load.fail.data"));
                });
                BasePlugin.logError(this.plugin.getLogger(), "load", "data", e.getPlayer().getUniqueId().toString(), var4);
            }

        });
    }

    @EventHandler
    public void onJoinStateCheck(PlayerJoinEvent e) {
        ListenableFutureUtils.addCallback(ListenableFutureUtils.addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), (user) -> {
            try {
                User.Complete userData = user.orElseThrow(Exception::new);

                String falseString = LangConfigurations.convertBoolean(this.i18n, false);

                if (userData.isVanished()) {
                    if (e.getPlayer().hasPermission("base.command.vanish")) {
                        userData.setVanished(true);
                    } else {
                        userData.setVanished(false);
                        e.getPlayer().sendMessage(MessageFormat.format(this.i18n.translate("vanished.player"), e.getPlayer().getDisplayName(), falseString));
                    }
                }

                if (userData.isGodModeEnabled()) {
                    if (!e.getPlayer().hasPermission("base.command.god")) {
                        userData.setGodModeEnabled(false);
                        e.getPlayer().sendMessage(MessageFormat.format(this.i18n.translate("god.mode"), e.getPlayer().getDisplayName(), falseString));
                    } else {
                        e.getPlayer().sendMessage(this.i18n.translate("player.in.god.mode"));
                    }
                }

                Collection player = Collections.singleton(e.getPlayer());
                Set<String> userIds = Bukkit.getOnlinePlayers().stream().map(OfflinePlayer::getUniqueId).map(UUID::toString).collect(Collectors.toSet());

                Bukkit.getScheduler().runTask(plugin, () -> {
                    this.findSync(userIds, userIds.size()).stream().filter(State::isVanished).forEach((userState) -> {
                        updateVanishState(Bukkit.getPlayer(userState.getUUID()), player, true);
                    });

                    this.save(userData);
                });
            } catch (Throwable var7) {
                Bukkit.getScheduler().runTask(this.plugin, () -> {
                    e.getPlayer().kickPlayer(this.i18n.translate("load.fail.data"));
                });
                BasePlugin.logError(this.plugin.getLogger(), "load", "data", e.getPlayer().getUniqueId().toString(), var7);
            }

        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e) {
        ListenableFutureUtils.addCallback(ListenableFutureUtils.addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), (user) -> {
            if (!user.isPresent()) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to find user data on quit event nick: {0} id: {1} ", new Object[]{e.getPlayer().getName(), e.getPlayer().getUniqueId()});
                return;
            }

            User.Complete userData = user.get();

            userData.setLastJoin(System.currentTimeMillis());
            userData.setOnline(false);

            if (userData.getNick() != null) {
                this.by_nickname.remove(userData.getNick());
            }

            this.save(userData);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent e) {
        addCallback(addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), (user) -> {

            if (!user.isPresent()) {
                e.getPlayer().sendMessage(this.i18n.translate("load.fail.data"));
                return;
            }

            User.Complete data = user.orElse(new BaseUser(e.getPlayer().getUniqueId()));

            data.setLastSpeakTime(System.currentTimeMillis());
            this.save(data);
        });
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntityType() == EntityType.PLAYER) {
            Player entity = (Player) e.getEntity();
            State state = this.findOneSync(entity.getUniqueId().toString());

            if (e.getCause() != DamageCause.VOID && state.isGodModeEnabled()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() == EntityType.PLAYER) {
            Player entity = (Player) e.getDamager();
            State state = this.findOneSync(entity.getUniqueId().toString());

            if (!entity.hasPermission("base.command.god.attack") && state.isGodModeEnabled()) {
                e.setCancelled(true);
                entity.sendMessage(this.i18n.translate("cant.damage.entities.in.god.mode"));
            }

            State damagerState = this.findOneSync(e.getDamager().getUniqueId().toString());
            if (state.isVanished() || damagerState.isVanished()) {
                e.setCancelled(true);
                if (e.getDamager().getType() == EntityType.PLAYER && damagerState.isVanished()) {
                    e.getDamager().sendMessage(this.i18n.translate("cant.damage.entities.vanished"));
                    return;
                }
            }

            if (state.isFreezed() || damagerState.isFreezed()) {
                e.setCancelled(true);
                if (e.getDamager().getType() == EntityType.PLAYER && damagerState.isFreezed()) {
                    e.getDamager().sendMessage(this.i18n.translate("cant.damage.entities.freezed"));
                }
            }

        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        State state = this.findOneSync(e.getPlayer().getUniqueId().toString());

        if (state.isVanished() || state.isFreezed()) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        State state = this.findOneSync(e.getPlayer().getUniqueId().toString());

        if (state.isVanished() || state.isFreezed()) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();

        State state = this.findOneSync(e.getPlayer().getUniqueId().toString());

        if ((from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) && state.isFreezed()) {
            to.setX(from.getX());
            to.setZ(from.getZ());
            e.setTo(to);
        }

    }
}
