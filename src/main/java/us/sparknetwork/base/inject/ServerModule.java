package us.sparknetwork.base.inject;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import lombok.AllArgsConstructor;
import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import us.sparknetwork.base.server.LocalServerData;

@AllArgsConstructor
public class ServerModule extends AbstractModule {

    private LocalServerData serverData;

    @Override
    protected void configure() {
        bind(us.sparknetwork.base.server.type.Server.class).to(LocalServerData.class);
        bind(LocalServerData.class).toInstance(serverData);
    }
}
