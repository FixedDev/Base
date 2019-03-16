package us.sparknetwork.base.punishment;

import com.google.common.util.concurrent.ListenableFuture;
import com.mongodb.lang.NonNullApi;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.user.User;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public interface PunishmentManager {
    @NotNull
    default Punishment createPunishment(@NotNull PunishmentType type,
                                        @NotNull CommandSender issuer,
                                        @NotNull User.AddressHistoryData punished,
                                        @NotNull String reason,
                                        @Nullable ZonedDateTime endDate,
                                        boolean ipPunishment,
                                        boolean silent) {
        return createPunishment(type, issuer, punished.getUUID(), punished.getLastName(), punished.getLastIp(), reason, endDate, ipPunishment, silent);
    }

    Punishment createPunishment(@NotNull PunishmentType type,
                                @NotNull CommandSender issuer,
                                @NotNull UUID punishedId,
                                @NotNull String punishedName,
                                @Nullable String punishedIp,
                                @NotNull String reason,
                                @Nullable ZonedDateTime endDate,
                                boolean ipPunishment,
                                boolean silent);

    void savePunishment(Punishment punishment);

    ListenableFuture<Punishment> getPunishmentById(@NotNull String id);

    @Nullable
    Punishment getPunishmentByIdSync(@NotNull String id);

    ListenableFuture<Punishment> getLastPunishment(@NotNull PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress);

    @Nullable
    Punishment getLastPunishmentSync(@NotNull PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress);

    ListenableFuture<List<Punishment>> getPunishments(@Nullable PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress, boolean active);

    @NotNull
    List<Punishment> getPunishmentsSync(@Nullable PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress, boolean active);

}
