package us.sparknetwork.base.user.session;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.UUID;

@AllArgsConstructor
public class SessionImpl implements Session {
    private final UUID playerId;
    private final ZonedDateTime joinTime;
    private String serverId;

    @NotNull
    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    @NotNull
    public ZonedDateTime joinTime() {
        return joinTime;
    }

    @NotNull
    @Override
    public String serverId() {
        return serverId;
    }

    @Override
    public String getId() {
        return playerId.toString();
    }
}
