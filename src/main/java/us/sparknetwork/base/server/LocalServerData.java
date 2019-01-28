package us.sparknetwork.base.server;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.sparknetwork.base.ServerConfigurations;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@JsonSerialize(as = Server.class)
public class LocalServerData implements Server {

    private String id;

    private String ip;
    private int port;

    @Setter
    private boolean online;

    private Instant startedAt;

    private LocalServerData(){
        startedAt = Instant.now();
    }

    public LocalServerData(String id, String ip, int port, boolean online) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.online = online;
        this.startedAt = Instant.now();
    }

    public LocalServerData(String id, String ip, int port, boolean online, Instant startedAt){
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.online = online;
        this.startedAt = startedAt;
    }

    @Override
    public Instant startedAt() {
        return startedAt;
    }

    @Override
    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public boolean isWhitelisted() {
        return Bukkit.hasWhitelist();
    }

    @Override
    public ServerRole getRole() {
        return ServerConfigurations.SERVER_ROLE;
    }

    @Override
    public ServerVisibility getVisibility() {
        return ServerConfigurations.SERVER_VISIBILIY;
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
        return Objects.hash(id, ip, port);
    }
}
