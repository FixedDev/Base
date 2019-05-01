package us.sparknetwork.base.restart;

import com.google.inject.Scopes;
import me.fixeddev.inject.ServiceBinder;
import us.sparknetwork.utils.inject.ProtectedModule;

public class RestartManagerModule extends ProtectedModule {
    @Override
    protected void configure() {
        bind(RestartManager.class).to(BaseRestartManager.class).in(Scopes.SINGLETON);

        ServiceBinder serviceBinder = new ServiceBinder(binder());
        serviceBinder.bindService(BaseRestartManager.class, Scopes.SINGLETON);
    }
}
