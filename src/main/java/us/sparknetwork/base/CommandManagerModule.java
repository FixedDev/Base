package us.sparknetwork.base;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import me.fixeddev.bcm.basic.BasicCommandHandler;
import me.fixeddev.bcm.basic.CommandDispatcher;
import me.fixeddev.bcm.basic.CommandRegistry;
import me.fixeddev.bcm.bukkit.BukkitCommandHandler;
import me.fixeddev.bcm.parametric.ParametricCommandHandler;
import me.fixeddev.bcm.parametric.ParametricCommandRegistry;
import org.bukkit.plugin.PluginLogger;

public class CommandManagerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CommandRegistry.class).to(BukkitCommandHandler.class);
        bind(CommandDispatcher.class).to(BukkitCommandHandler.class);

        bind(ParametricCommandRegistry.class).to(BukkitCommandHandler.class);
        bind(BasicCommandHandler.class).to(BukkitCommandHandler.class);

        bind(ParametricCommandHandler.class).to(BukkitCommandHandler.class);
    }

    @Provides
    @Singleton
    public BukkitCommandHandler getBukkitCommandHandler(PluginLogger logger) {
        return new BukkitCommandHandler(logger, null);
    }
}
