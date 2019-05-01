package us.sparknetwork.base.server;

import com.google.inject.Scopes;
import me.fixeddev.inject.ProtectedModule;
import me.fixeddev.inject.ServiceBinder;

public class ServerManagerModule extends ProtectedModule {
    @Override
    protected void configure() {
        bind(ServerManager.class).to(MongoServerManager.class);

        ServiceBinder serviceBinder = new ServiceBinder(binder());
        serviceBinder.bindService(MongoServerManager.class, Scopes.SINGLETON);

        expose(ServerManager.class);
    }
}
