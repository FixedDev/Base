package us.sparknetwork.base.handlers.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import us.sparknetwork.base.datamanager.Model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@JsonDeserialize(as = ServerData.class)
public interface Server extends Model {

    String getDisplayName();

    String getIp();

    int getPort();

    Instant startedAt();

    boolean isOnline();

    boolean isWhitelisted();

    Set<String> getOnlinePlayerNicks();

    Set<UUID> getOnlinePlayerIds();

    int getMaxPlayers();

    default boolean equals(Server o){
        return this.getId().equals(o.getId());
    }
}
