package us.sparknetwork.base.handlers.user.state;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.redisson.api.RedissonClient;
import us.sparknetwork.base.I18n;
import us.sparknetwork.base.LangConfigurations;
import us.sparknetwork.base.StaffPriority;
import us.sparknetwork.base.datamanager.CachedMongoStorageProvider;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class UserStateHandler extends CachedMongoStorageProvider<UserState> implements Listener {

    @Inject
    private I18n i18n;

    @Inject
    UserStateHandler(ListeningExecutorService executorService, MongoDatabase database, RedissonClient redisson) {
        super(executorService, database, redisson, "user.state", UserState.class);
    }

    static void updateVanishState(Player player, Collection<? extends Player> collection, boolean vanished) {
        player.spigot().setCollidesWithEntities(!vanished);
        for (Player viewer : collection) {
            if (player.equals(viewer))
                continue;
            if (vanished) {
                if (StaffPriority.getByPlayer(player).isMoreThan(StaffPriority.getByPlayer(viewer))) {
                    viewer.hidePlayer(player);
                }
                continue;
            }
            viewer.showPlayer(player);
        }

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        UUID playerId = e.getPlayer().getUniqueId();
        UserState state = Optional.ofNullable(findOneSync(playerId.toString())).orElse(new BaseUserState(playerId));

        String falseString = LangConfigurations.convertBoolean(i18n, false);

        if (state.isVanished()) {
            if (e.getPlayer().hasPermission("base.command.vanish")) {
                updateVanishState(e.getPlayer(), Bukkit.getOnlinePlayers(), true);
            } else {
                state.setVanished(false);

                e.getPlayer().sendMessage(MessageFormat.format(i18n.translate("vanished.player"), e.getPlayer().getDisplayName(), falseString));
            }

        }

        if (state.isGodModeEnabled()) {
            if (!e.getPlayer().hasPermission("base.command.god")) {
                state.setGodModeEnabled(false);

                e.getPlayer().sendMessage(MessageFormat.format(i18n.translate("god.mode"), e.getPlayer().getDisplayName(), falseString));
            } else {
                e.getPlayer().sendMessage(i18n.translate("player.in.god.mode"));
            }
        }

        Collection<? extends Player> player = Collections.singleton(e.getPlayer());

        Set<String> userIds = Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).map(UUID::toString).collect(Collectors.toSet());

        findSync(userIds, userIds.size()).stream().filter(UserState::isVanished).forEach(userState -> updateVanishState(Bukkit.getPlayer(userState.getUniqueId()), player, true));
        save(state);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntityType() != EntityType.PLAYER) return;

        Player entity = (Player) e.getEntity();

        UserState state = findOneSync(entity.getUniqueId().toString());

        if (e.getCause() != EntityDamageEvent.DamageCause.VOID && state.isGodModeEnabled()) {
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType() != EntityType.PLAYER) return;

        Player entity = (Player) e.getDamager();

        UserState state = findOneSync(entity.getUniqueId().toString());

        if (!entity.hasPermission("base.command.god.attack") && state.isGodModeEnabled()) {
            e.setCancelled(true);
            entity.sendMessage(i18n.translate("cant.damage.entities.in.god.mode"));
        }

        UserState damagerState = findOneSync(e.getDamager().getUniqueId().toString());

        if (state.isVanished() || damagerState.isVanished()) {
            e.setCancelled(true);
            if (e.getDamager().getType() == EntityType.PLAYER && damagerState.isVanished()) {
                e.getDamager().sendMessage(i18n.translate("cant.damage.entities.vanished"));
                return;
            }
        }

        if (state.isFreezed() || damagerState.isFreezed()) {
            e.setCancelled(true);
            if (e.getDamager().getType() == EntityType.PLAYER && damagerState.isFreezed()) {
                e.getDamager().sendMessage(i18n.translate("cant.damage.entities.freezed"));
            }
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        UserState state = findOneSync(e.getPlayer().getUniqueId().toString());

        if (state.isVanished() || state.isFreezed()) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        UserState state = findOneSync(e.getPlayer().getUniqueId().toString());

        if (state.isVanished() || state.isFreezed()) {
            e.setCancelled(true);
        }
    }


    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();

        UserState state = findOneSync(e.getPlayer().getUniqueId().toString());

        if ((from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) && state.isFreezed()) {
            to.setX(from.getX());
            to.setZ(from.getZ());
            e.setTo(to);
        }
    }
}
