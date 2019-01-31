package us.sparknetwork.base.server.type;

import java.util.Set;
import java.util.UUID;

public interface PrivateServer extends Server {
    Set<UUID> getAllowedUserIds();
}
