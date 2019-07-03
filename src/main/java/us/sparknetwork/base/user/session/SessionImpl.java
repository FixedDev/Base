package us.sparknetwork.base.user.session;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@AllArgsConstructor
public class SessionImpl implements Session {
    private UUID playerId;
    private String serverId;

    @NotNull
    @Override
    public UUID playerId() {
        return playerId;
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
