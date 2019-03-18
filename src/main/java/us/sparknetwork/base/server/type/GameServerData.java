package us.sparknetwork.base.server.type;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import us.sparknetwork.base.restart.RestartPriority;
import us.sparknetwork.base.server.ServerRole;
import us.sparknetwork.base.server.ServerVisibility;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class GameServerData extends LocalGameServer {

    private boolean whitelisted;

    private ServerRole role;
    private ServerVisibility visibility;

    private Set<String> onlinePlayerNicks;
    private Set<UUID> onlinePlayerIds;

    private int maxPlayers;

    @JsonCreator
    GameServerData(@JsonProperty("_id") String id,
                   @JsonProperty("ip") String ip,
                   @JsonProperty("port") int port,
                   @JsonProperty("startedAt") Instant startedAt,
                   @JsonProperty("nextRestartDate") LocalDateTime dateTime,
                   @JsonProperty("nextRestartPriority") RestartPriority restartPriority,
                   @JsonProperty("gameId") String gameId,
                   @JsonProperty("online") boolean online,
                   @JsonProperty("whitelisted") boolean whitelisted,
                   @JsonProperty("role") ServerRole serverRole,
                   @JsonProperty("visibility") ServerVisibility visibility,
                   @JsonProperty("onlinePlayerNicks") Set<String> onlinePlayerNicks,
                   @JsonProperty("onlinePlayerIds") Set<UUID> onlinePlayerIds,
                   @JsonProperty("maxPlayers") int maxPlayers,
                   @JsonProperty("gamePlayers") Set<UUID> gamePlayers,
                   @JsonProperty("spectators") Set<UUID> spectators) {
        super(id, ip, port, online, startedAt, dateTime, restartPriority, gameId);
        this.whitelisted = whitelisted;
        this.role = serverRole;
        this.visibility = visibility;
        this.onlinePlayerNicks = onlinePlayerNicks;
        this.onlinePlayerIds = onlinePlayerIds;
        this.maxPlayers = maxPlayers;

        setGamePlayers(gamePlayers);
        setSpectators(spectators);
    }

    @Override
    public boolean isWhitelisted() {
        return whitelisted;
    }

    @Override
    @NotNull
    public ServerRole getRole() {
        return role;
    }

    @Override
    @NotNull
    public ServerVisibility getVisibility() {
        return visibility;
    }

    @Override
    @NotNull
    public Set<String> getOnlinePlayerNicks() {
        return onlinePlayerNicks;
    }

    @Override
    @NotNull
    public Set<UUID> getOnlinePlayerIds() {
        return onlinePlayerIds;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }
}
