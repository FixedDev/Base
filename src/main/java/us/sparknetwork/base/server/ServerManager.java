package us.sparknetwork.base.server;

import us.sparknetwork.base.Service;
import us.sparknetwork.base.datamanager.StorageProvider;
import us.sparknetwork.base.server.type.Server;

public interface ServerManager extends StorageProvider<Server>, Service {
}
