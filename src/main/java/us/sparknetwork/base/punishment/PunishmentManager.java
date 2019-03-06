package us.sparknetwork.base.punishment;

import com.google.common.util.concurrent.ListenableFuture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.user.Identity;
import us.sparknetwork.base.user.User;

import java.time.Instant;
import java.util.UUID;

public interface PunishmentManager {
    @NotNull
    Punishment createPunishment(@NotNull PunishmentType type, @NotNull Identity issuer, @NotNull User.AddressHistoryData punished, @NotNull String reason, @Nullable Instant endDate, boolean ipPunishment);

    ListenableFuture<Punishment> getPunishmentById(@NotNull String id);

    @Nullable
    Punishment getPunishmentByIdSync(@NotNull String id);

    ListenableFuture<Punishment> getPunishment(@NotNull PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress);

    @Nullable
    Punishment getPunishmentSync(@NotNull PunishmentType type, @Nullable UUID playerId, @Nullable String playerAddress);

}
