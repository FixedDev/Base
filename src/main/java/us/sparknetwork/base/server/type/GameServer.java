package us.sparknetwork.base.server.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public interface GameServer extends Server.Partial {
    @NotNull String getGameId();

    @Nullable Set<UUID> getSpectators();

    @Nullable Set<UUID> getGamePlayers();
}
