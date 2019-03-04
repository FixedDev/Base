package us.sparknetwork.base.server.type;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.sparknetwork.base.api.datamanager.Model;
import us.sparknetwork.base.api.restart.RestartPriority;
import us.sparknetwork.base.server.ServerData;
import us.sparknetwork.base.server.ServerRole;
import us.sparknetwork.base.server.ServerVisibility;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@JsonDeserialize(as = ServerData.class)
public interface Server extends Model {

    String getIp();

    int getPort();

    @Nullable
    Instant startedAt();

    void setStartedAt(@Nullable Instant startedAt);

    boolean isOnline();

    boolean isWhitelisted();

    @NotNull
    ServerRole getRole();

    @NotNull
    ServerVisibility getVisibility();

    @Nullable
    LocalDateTime getNextRestartDate();

    @Nullable
    RestartPriority getNextRestartPriority();

    void setNextRestartDate(@Nullable LocalDateTime date);

    void setNextRestartPriority(@NotNull RestartPriority priority);

    @NotNull
    Set<String> getOnlinePlayerNicks();

    @NotNull
    Set<UUID> getOnlinePlayerIds();

    int getMaxPlayers();

    default boolean equals(Server o) {
        return this.getId().equals(o.getId());
    }
}


