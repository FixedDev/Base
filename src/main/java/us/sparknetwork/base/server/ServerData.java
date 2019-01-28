package us.sparknetwork.base.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonSerialize(as = Server.class)
@JsonIgnoreProperties("displayName")
public class ServerData extends LocalServerData implements Server {

    private boolean whitelisted;

    private ServerRole role;
    private ServerVisibility visibility;

    private Set<String> onlinePlayerNicks;
    private Set<UUID> onlinePlayerIds;

    private int maxPlayers;

    @JsonCreator
    ServerData(@JsonProperty("_id") String id,
               @JsonProperty("ip") String ip,
               @JsonProperty("port") int port,
               @JsonProperty("startedAt") Instant startedAt,
               @JsonProperty("online") boolean online,
               @JsonProperty("whitelisted") boolean whitelisted,
               @JsonProperty("role") ServerRole serverRole,
               @JsonProperty("visibility") ServerVisibility visibility,
               @JsonProperty("onlinePlayerNicks") Set<String> onlinePlayerNicks,
               @JsonProperty("onlinePlayerIds") Set<UUID> onlinePlayerIds,
               @JsonProperty("maxPlayers") int maxPlayers) {
        super(id, ip, port, online, startedAt);
        this.whitelisted = whitelisted;
        this.role = serverRole;
        this.visibility = visibility;
        this.onlinePlayerNicks = onlinePlayerNicks;
        this.onlinePlayerIds = onlinePlayerIds;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public boolean isWhitelisted() {
        return whitelisted;
    }

    @Override
    public ServerRole getRole() {
        return role;
    }

    @Override
    public ServerVisibility getVisibility() {
        return visibility;
    }

    @Override
    public Set<String> getOnlinePlayerNicks() {
        return onlinePlayerNicks;
    }

    @Override
    public Set<UUID> getOnlinePlayerIds() {
        return onlinePlayerIds;
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Server)) return false;
        Server that = (Server) o;
        return equals(that);
    }
}
