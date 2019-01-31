package us.sparknetwork.base.server;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.ServerConfigurations;
import us.sparknetwork.base.restart.RestartPriority;
import us.sparknetwork.base.server.type.Server;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@JsonSerialize(as = Server.class)
public class LocalServerData implements Server {

    @NotNull
    private String id;

    @NotNull
    private String ip;
    private int port;

    @Setter
    private boolean online;

    @Nullable
    private Instant startedAt;

    @Nullable
    private LocalDateTime restartDate;
    @Nullable
    private RestartPriority restartPriority;

    private LocalServerData() {
        startedAt = Instant.now();
    }

    public LocalServerData(String id, String ip, int port, boolean online) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.online = online;
        this.startedAt = Instant.now();
    }

    public LocalServerData(@NotNull String id,
                           @NotNull String ip,
                           int port,
                           boolean online,
                           @Nullable Instant startedAt,
                           @Nullable LocalDateTime restartDate,
                           @Nullable RestartPriority restartPriority) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.online = online;
        this.startedAt = startedAt;

        this.restartDate = restartDate;
        this.restartPriority = restartPriority;
    }

    @Override
    @Nullable
    public Instant startedAt() {
        return startedAt;
    }

    @Override
    public void setStartedAt(@Nullable Instant startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public boolean isWhitelisted() {
        return Bukkit.hasWhitelist();
    }

    @Override
    @NotNull
    public ServerRole getRole() {
        return ServerConfigurations.SERVER_ROLE;
    }

    @Override
    @NotNull
    public ServerVisibility getVisibility() {
        return ServerConfigurations.SERVER_VISIBILIY;
    }

    @Override
    @Nullable
    public LocalDateTime getNextRestartDate() {
        return restartDate;
    }

    @Override
    @Nullable
    public RestartPriority getNextRestartPriority() {
        return restartPriority;
    }

    @Override
    public void setNextRestartDate(@Nullable LocalDateTime date) {
        restartDate = date;
    }

    @Override
    public void setNextRestartPriority(@NotNull RestartPriority priority) {
        restartPriority = priority;
    }

    @Override
    @NotNull
    public Set<String> getOnlinePlayerNicks() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet());
    }

    @Override
    @NotNull
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
