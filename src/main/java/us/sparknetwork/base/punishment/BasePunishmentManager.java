package us.sparknetwork.base.punishment;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.BasePlugin;
import us.sparknetwork.base.datamanager.MongoStorageProvider;
import us.sparknetwork.base.event.PunishmentEvent;
import us.sparknetwork.base.id.IdGenerator;
import us.sparknetwork.base.user.User;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.mongodb.client.model.Filters.*;

@Singleton
public class BasePunishmentManager extends MongoStorageProvider<Punishment> implements PunishmentManager {
    private IdGenerator idGenerator;
    private ListeningExecutorService executorService;

    @Inject
    private JavaPlugin plugin;

    @Inject
    BasePunishmentManager(ListeningExecutorService executorService, MongoDatabase database, IdGenerator generator) {
        super(executorService, database, "punishments", Punishment.class);
        idGenerator = generator;
        this.executorService = executorService;
    }

    @NotNull
    @Override
    public Punishment createPunishment(@NotNull PunishmentType type, @NotNull CommandSender issuer, @NotNull User.AddressHistoryData punished, @NotNull String reason, ZonedDateTime endDate, boolean ipPunishment, boolean silent) {
        UUID uniqueId = BasePlugin.CONSOLE_UUID;

        if (issuer instanceof Player) {
            uniqueId = ((Player) issuer).getUniqueId();
        }


        if (type == PunishmentType.BAN || type == PunishmentType.MUTE) {
            Punishment oldPunishment = getLastPunishmentSync(type, punished.getUUID(), punished.getLastIp());

            if (oldPunishment != null) {
                oldPunishment.setActive(false);
                save(oldPunishment);
            }
        }


        BasePunishment punishment = new BasePunishment(
                idGenerator.getNextId("punishments") + "",
                uniqueId,
                issuer.getName(),
                punished.getUUID(),
                punished.getLastName(),
                punished.getLastIp(),
                type,
                reason,
                ZonedDateTime.now(),
                endDate,
                ipPunishment,
                true,
                silent);

        save(punishment);

        // The punishment event should be called in the main thread only
        Bukkit.getScheduler().runTask(plugin, () -> {
            PunishmentEvent event = new PunishmentEvent(punishment);
            Bukkit.getPluginManager().callEvent(event);
        });

        return punishment;
    }

    @Override
    public void savePunishment(Punishment punishment) {
        save(punishment);
    }

    @Override
    public ListenableFuture<Punishment> getPunishmentById(@NotNull String id) {
        return findOne(id);
    }

    @Override
    public @Nullable Punishment getPunishmentByIdSync(@NotNull String id) {
        return findOneSync(id);
    }

    @Override
    public ListenableFuture<Punishment> getLastPunishment(@NotNull PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress) {
        if (playerId == null && playerAddress == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("The player id or the player address must be not null!"));
        }

        return executorService.submit(() -> getLastPunishmentSync(type, playerId, playerAddress));
    }

    @Override
    public @Nullable Punishment getLastPunishmentSync(@NotNull PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress) {
        Bson basicQuery = and(
                eq("active", true),
                eq("type", type.toString()));

        if (playerId != null) {
            Iterator<Punishment> iterator = findByQuerySync(and(
                    eq("punishedId", playerId.toString()),
                    basicQuery), 0, 1).iterator();

            if (iterator.hasNext()) {
                return iterator.next();
            }
        }


        if (!StringUtils.isBlank(playerAddress)) {
            Iterator<Punishment> iterator = findByQuerySync(and(
                    eq("punishedAddress", playerAddress),
                    eq("ipPunishment", true),
                    basicQuery), 0, 1).iterator();

            if (iterator.hasNext()) {
                return iterator.next();
            }
        }

        return null;
    }

    @Override
    public ListenableFuture<List<Punishment>> getPunishments(@Nullable PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress, boolean active) {
        if (playerId == null && playerAddress == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("The player id or the player address must be not null!"));
        }

        return executorService.submit(() -> getPunishmentsSync(type, playerId, playerAddress, active));
    }

    @Override
    public @NotNull List<Punishment> getPunishmentsSync(@Nullable PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress, boolean active) {
        List<Punishment> punishments = new ArrayList<>();

        Bson basicQuery = eq("active", active);

        if (type != null) {
            basicQuery = and(basicQuery, eq("type", type.toString()));
        }

        if (playerId != null) {
            punishments.addAll(findByQuerySync(and(
                    basicQuery,
                    eq("punishedId", playerId.toString())), 0, Integer.MAX_VALUE));
        }


        if (!StringUtils.isBlank(playerAddress)) {
            punishments.addAll(findByQuerySync(and(
                    basicQuery,
                    eq("punishedAddress", playerAddress),
                    eq("ipPunishment", true)), 0, Integer.MAX_VALUE));
        }

        return punishments;
    }
}
