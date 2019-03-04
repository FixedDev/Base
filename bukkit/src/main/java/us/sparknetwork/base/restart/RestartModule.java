package us.sparknetwork.base.restart;

import com.google.inject.AbstractModule;
import us.sparknetwork.base.api.restart.RestartManager;

public class RestartModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(RestartManager.class).to(BaseRestartManager.class);
    }
}
