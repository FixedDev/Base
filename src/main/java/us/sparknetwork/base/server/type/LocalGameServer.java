package us.sparknetwork.base.server.type;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.restart.RestartPriority;
import us.sparknetwork.base.server.LocalServerData;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@JsonSerialize(as = GameServer.class)
public class LocalGameServer extends LocalServerData implements GameServer {

    @NotNull
    private String gameId;

    @Nullable
    private Set<UUID> spectators;
    @Nullable
    private Set<UUID> gamePlayers;

    public LocalGameServer(String id, String ip, int port, @NotNull String gameId) {
        super(id, ip, port, true);

        this.gameId = gameId;
    }

    public LocalGameServer(@NotNull String id,
                           @NotNull String ip,
                           int port,
                           boolean online,
                           @Nullable Instant startedAt,
                           @Nullable LocalDateTime restartDate,
                           @Nullable RestartPriority restartPriority,
                           @NotNull String gameId) {
        super(id, ip, port, online, startedAt, restartDate, restartPriority);

        this.gameId = gameId;
    }

    @NotNull
    @Override
    public String getGameId() {
        return gameId;
    }

    @Nullable
    @Override
    public synchronized Set<UUID> getSpectators() {
        return spectators;
    }

    public synchronized void setSpectators(@Nullable Set<UUID> spectators) {
        this.spectators = spectators;
    }

    @Nullable
    @Override
    public synchronized Set<UUID> getGamePlayers() {
        return gamePlayers;
    }

    public synchronized void setGamePlayers(@Nullable Set<UUID> gamePlayers) {
        this.gamePlayers = gamePlayers;
    }

}
