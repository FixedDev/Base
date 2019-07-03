package us.sparknetwork.base.user;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.mongodb.client.MongoDatabase;

import java.text.MessageFormat;
import java.util.*;
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
import org.bukkit.event.player.*;
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

public class BaseUserHandler extends CachedMongoStorageProvider<User.Complete, User.Partial> implements UserHandler {
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
                    if (StaffPriority.getByCommandSender(player).isMoreThan(StaffPriority.getByCommandSender(viewer))) {
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


    @EventHandler(priority = EventPriority.LOW)
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        ListenableFutureUtils.addCallback(ListenableFutureUtils.addOptionalToReturnValue(this.findOne(e.getUniqueId().toString())), (user) -> {
            User.Complete userData = user.orElse(new BaseUser(e.getUniqueId()));

            // Save in cache the userData and/or create the userdata
            this.save(userData);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent e) {
        // Player is already in redis, should be not problem to load it sync
        Optional<User.Complete> optionalUser = Optional.ofNullable(findOneSync(e.getPlayer().getUniqueId().toString()));

        if (!optionalUser.isPresent()) {
            e.disallow(Result.KICK_OTHER, i18n.translate("load.fail.data"));
            return;
        }

        try {
            User.Complete userData = optionalUser.get();
            userData.setLastServerId(Bukkit.getServerName());
            userData.setOnline(true);

            this.save(userData);
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "There was an exception while handling user " + e.getPlayer().getUniqueId().toString() + " login data", ex);
        }

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
                        //  e.getPlayer().setPlayerListName(userData.getNick());

                        this.by_nickname.put(userData.getNick(), userData.getUUID());
                    } else {

                        userData.setNick(null);

                        e.getPlayer().setDisplayName(userData.getLastName());
                        //  e.getPlayer().setPlayerListName(userData.getLastName());
                    }
                }

                List<String> availableLimits = new ArrayList<>();

                e.getPlayer().getEffectivePermissions().forEach(permissionAttachmentInfo -> {
                    String permission = permissionAttachmentInfo.getPermission();

                    if (!permission.startsWith("base.friends.limit") || !permissionAttachmentInfo.getValue()) {
                        return;
                    }

                    String limit = permission.replace("base.friends.limit", "");

                    availableLimits.add(limit);
                });

                if (availableLimits.contains("unlimited")) {
                    userData.setFriendsLimit(Integer.MAX_VALUE);
                } else if (availableLimits.isEmpty()) {
                    userData.setFriendsLimit(5);
                } else {
                    availableLimits.stream().map(s -> {
                        try {
                            return Integer.parseInt(s);
                        } catch (NumberFormatException ex) {
                            return 5;
                        }
                    }).sorted(Integer::compare).forEachOrdered(userData::setFriendsLimit);
                }

                userData.setLastJoin(System.currentTimeMillis());

                checkState(userData, e);

                this.save(userData);
            } catch (Exception ex) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        e.getPlayer().kickPlayer(this.i18n.translate("load.fail.data"))
                );
                plugin.getLogger().log(Level.SEVERE, "There was an exception while handling user " + e.getPlayer().getUniqueId().toString() + " data", ex);
            }
        });

    }

    private void checkState(State userData, PlayerJoinEvent e) {
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

        Bukkit.getScheduler().runTask(plugin, () ->
                this.findSync(userIds, userIds.size()).stream().filter(State::isVanished).forEach((userState) -> {
                    updateVanishState(Bukkit.getPlayer(userState.getUUID()), player, true);
                })
        );
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeave(PlayerQuitEvent e) {
        ListenableFutureUtils.addCallback(ListenableFutureUtils.addOptionalToReturnValue(this.findOne(e.getPlayer().getUniqueId().toString())), (user) -> {
            if (!user.isPresent()) {
                this.plugin.getLogger().log(Level.SEVERE, "Failed to find user data on quit event nick: {0} id: {1} ", new Object[]{e.getPlayer().getName(), e.getPlayer().getUniqueId()});
                return;
            }

            User.Complete userData = user.get();

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

            User.ChatData data = user.get();

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
