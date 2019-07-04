package us.sparknetwork.base.user.session;

import me.fixeddev.inject.ProtectedModule;

public class SessionModule extends ProtectedModule {
    @Override
    protected void configure() {
        bind(SessionHandler.class).to(SessionHandlerImpl.class);

        expose(SessionHandler.class);
    }
}
