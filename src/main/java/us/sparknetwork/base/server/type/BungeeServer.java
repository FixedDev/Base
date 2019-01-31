package us.sparknetwork.base.server.type;

import java.util.List;

public interface BungeeServer extends Server {
    String getServerMotd();

    List<Server> getRegisteredServers();
}
