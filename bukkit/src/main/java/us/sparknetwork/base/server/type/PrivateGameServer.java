package us.sparknetwork.base.server.type;

import java.util.UUID;

public interface PrivateGameServer extends GameServer, PrivateServer {
    UUID getServerOwner();
}
