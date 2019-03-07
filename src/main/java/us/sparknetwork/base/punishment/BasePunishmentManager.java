package us.sparknetwork.base.punishment;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.BasePlugin;
import us.sparknetwork.base.datamanager.MongoStorageProvider;
import us.sparknetwork.base.event.PunishmentEvent;
import us.sparknetwork.base.id.IdGenerator;
import us.sparknetwork.base.user.User;

import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

import static com.mongodb.client.model.Filters.*;

@Singleton
public class BasePunishmentManager extends MongoStorageProvider<Punishment> implements PunishmentManager {
    private IdGenerator idGenerator;
    private ListeningExecutorService executorService;

    @Inject
    BasePunishmentManager(ListeningExecutorService executorService, MongoDatabase database, IdGenerator generator) {
        super(executorService, database, "punishments", Punishment.class);
        idGenerator = generator;
        this.executorService = executorService;
    }

    @NotNull
    @Override
    public Punishment createPunishment(@NotNull PunishmentType type, @NotNull CommandSender issuer, @NotNull User.AddressHistoryData punished, @NotNull String reason, Instant endDate, boolean ipPunishment, boolean silent) {
        UUID uniqueId = BasePlugin.CONSOLE_UUID;

        if(issuer instanceof Player){
            uniqueId = ((Player) issuer).getUniqueId();
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
                Instant.now(),
                endDate,
                ipPunishment,
                true,
                silent);

        save(punishment);

        PunishmentEvent event = new PunishmentEvent(punishment);
        Bukkit.getPluginManager().callEvent(event);

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
    public ListenableFuture<Punishment> getPunishment(@NotNull PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress) {
        if (playerId == null && playerAddress == null) {
            return Futures.immediateFailedFuture(new IllegalArgumentException("The player id or the player address must be not null!"));
        }

        return executorService.submit(() -> getPunishmentSync(type, playerId, playerAddress));
    }

    @Override
    public @Nullable Punishment getPunishmentSync(@NotNull PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress) {
        if (playerId != null) {
            Iterator<Punishment> iterator = findByQuerySync(and(eq("active", true),
                    eq("punishedId", playerId),
                    eq("type", type)), 0, 1).iterator();

            if (iterator.hasNext()) {
                return iterator.next();
            }
        }

        if (!StringUtils.isBlank(playerAddress)) {
            Iterator<Punishment> iterator = findByQuerySync(and(eq("active", true),
                    eq("punishedAddress", playerAddress),
                    eq("type", type)), 0, 1).iterator();

            if (iterator.hasNext()) {
                Punishment tempPunish = iterator.next();

                if(tempPunish.isIpPunishment()){
                   return tempPunish;
                }

            }
        }

        return null;
    }
}
