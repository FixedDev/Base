package us.sparknetwork.base.server;

import us.sparknetwork.base.api.Service;
import us.sparknetwork.base.api.datamanager.StorageProvider;
import us.sparknetwork.base.server.type.Server;

public interface ServerManager extends StorageProvider<Server>, Service {
}
