package us.sparknetwork.base.handlers.server;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonSerialize(as = Server.class)
public class ServerData extends LocalServerData implements Server {

    private String id;
    private String displayName;

    private String ip;
    private int port;

    private Instant startedAt;

    private boolean online;
    private boolean whitelisted;

    private Set<String> onlinePlayerNicks;
    private Set<UUID> onlinePlayerIds;

    private int maxPlayers;

    @JsonCreator
    ServerData(@JsonProperty("_id") String id,
               @JsonProperty("displayName") String displayName,
               @JsonProperty("ip") String ip,
               @JsonProperty("port") int port,
               @JsonProperty("startedAt") Instant startedAt,
               @JsonProperty("online") boolean online,
               @JsonProperty("whitelisted") boolean whitelisted,
               @JsonProperty("onlinePlayerNicks") Set<String> onlinePlayerNicks,
               @JsonProperty("onlinePlayerIds") Set<UUID> onlinePlayerIds,
               @JsonProperty("maxPlayers") int maxPlayers) {
        super(id, displayName, ip, port, online);
        this.id = id;
        this.displayName = displayName;
        this.ip = ip;
        this.port = port;
        this.startedAt = startedAt;
        this.online = online;
        this.whitelisted = whitelisted;
        this.onlinePlayerNicks = onlinePlayerNicks;
        this.onlinePlayerIds = onlinePlayerIds;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getIp() {
        return ip;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Instant startedAt() {
        return startedAt;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public boolean isWhitelisted() {
        return whitelisted;
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
