package us.sparknetwork.base.server.type;

import java.util.Set;
import java.util.UUID;

public interface GameServer extends Server {
    Set<UUID> getSpectators();

    Set<UUID> getGamePlayers();
}
