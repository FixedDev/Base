package us.sparknetwork.base.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import us.sparknetwork.base.datamanager.Model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonDeserialize(as = ServerData.class)
public interface Server extends Model {

    String getIp();

    int getPort();

    Instant startedAt();

    void setStartedAt(Instant startedAt);

    boolean isOnline();

    boolean isWhitelisted();

    ServerRole getRole();

    ServerVisibility getVisibility();

    Set<String> getOnlinePlayerNicks();

    Set<UUID> getOnlinePlayerIds();

    int getMaxPlayers();

    default boolean equals(Server o){
        return this.getId().equals(o.getId());
    }
}

