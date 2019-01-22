package us.sparknetwork.base.handlers.server;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@JsonSerialize(as = Server.class)
public class LocalServerData implements Server {

    private String id;
    private String displayName;

    private String ip;
    private int port;

    @Setter
    private boolean online;

    private Instant startedAt;

    private LocalServerData(){
        startedAt = Instant.now();
    }

    public LocalServerData(String id, String displayName, String ip, int port, boolean online) {
        this.id = id;
        this.displayName = displayName;
        this.ip = ip;
        this.port = port;
        this.online = online;
        this.startedAt = Instant.now();
    }

    @Override
    public Instant startedAt() {
        return startedAt;
    }

    @Override
    public boolean isWhitelisted() {
        return Bukkit.hasWhitelist();
    }

    @Override
    public Set<String> getOnlinePlayerNicks() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet());
    }

    @Override
    public Set<UUID> getOnlinePlayerIds() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(Collectors.toSet());
    }

    @Override
    public int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Server)) return false;
        Server that = (Server) o;
        return equals(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, ip, port);
    }
}
