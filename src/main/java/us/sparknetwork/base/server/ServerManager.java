package us.sparknetwork.base.server;

import me.fixeddev.service.PluginService;
import me.fixeddev.service.Service;
import us.sparknetwork.base.datamanager.StorageProvider;
import us.sparknetwork.base.server.type.Server;

public interface ServerManager extends StorageProvider<Server, Server>, PluginService {
}
