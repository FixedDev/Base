package us.sparknetwork.base.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import us.sparknetwork.cm.CommandHandler;
import us.sparknetwork.cm.handlers.PreProcessCommandHandler;

public class CommandHandlerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CommandHandler.class).to(PreProcessCommandHandler.class);
        bind(PreProcessCommandHandler.class).in(Scopes.SINGLETON);
    }
}
